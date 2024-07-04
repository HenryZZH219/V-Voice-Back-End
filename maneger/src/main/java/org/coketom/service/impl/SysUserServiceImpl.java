package org.coketom.service.impl;

import com.alibaba.fastjson.JSON;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.servlet.http.HttpSession;
import org.coketom.AuthContextUtil;
import org.coketom.dto.system.LoginDto;
import org.coketom.dto.system.PasswdDto;
import org.coketom.entity.system.SysUser;
import org.coketom.exception.TomException;
import org.coketom.mapper.SysUserMapper;
import org.coketom.properties.MinioProperties;
import org.coketom.service.RedisService;
import org.coketom.service.SysUserService;
import org.coketom.vo.common.ResultCodeEnum;
import org.coketom.vo.system.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisService redisService;
    @Override
    public LoginVo login(LoginDto loginDto) {
//        System.out.println(loginDto);
        String userName = loginDto.getUsername();
        SysUser sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        if(sysUser == null){
            throw new TomException(ResultCodeEnum.LOGIN_ERROR);
        }

        String password = sysUser.getPassword();
        String input_password =
                DigestUtils.md5DigestAsHex(loginDto.getPassword().getBytes());

        if(!password.equals(input_password)){
            throw new TomException(ResultCodeEnum.LOGIN_ERROR);
        }

        String token = UUID.randomUUID().toString().replaceAll("-","");

        redisService.addToken(token, sysUser);
//        redisTemplate.opsForValue()
//                .set("user:login"+token,
//                        JSON.toJSONString(sysUser),
//                        30,
//                        TimeUnit.MINUTES);
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setSysUser(sysUser);

        return loginVo;


    }

    @Override
    public void register(SysUser sysUser) {

        String userName = sysUser.getUsername();
        SysUser _sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        if(_sysUser != null){
            throw new TomException(ResultCodeEnum.USER_NAME_IS_EXISTS);
        }
        String password = DigestUtils.md5DigestAsHex(sysUser.getPassword().getBytes());
        sysUser.setPassword(password);
        sysUser.setAvatar(getFirstSegment(userName));
        sysUserMapper.register(sysUser);
    }

    public static String getFirstSegment(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        // 使用正则表达式匹配第一个单词或第一个非英文字符
        String[] words = str.split("\\s+"); // 以空白字符分割字符串
        for (String word : words) {
            // 检查每个单词
            if (word.matches("^[a-zA-Z0-9]+")) {
                // 如果是英文或数字单词，返回它
                return word;
            } else {
                // 否则，遍历这个单词中的每个字符，找到第一个非英文字符
                for (char c : word.toCharArray()) {
                    if (!Character.isLetterOrDigit(c)) {
                        return String.valueOf(c);
                    }
                }
            }
        }

        // 如果没有匹配到任何非英文字符或单词，则返回空字符串
        return "";
    }

    @Override
    public void logout(String token) {
        redisService.deleteToken(token);
    }

    @Override
    public SysUser getuserinfo(String token) {
        return AuthContextUtil.get();
    }

    @Override
    public void updateUser(String token, SysUser sysUser) {
        Integer userId = AuthContextUtil.get().getId();
        if(sysUser.getName() != null){
            sysUserMapper.setName(userId, sysUser.getName());
        }
        if(sysUser.getPhone() != null){
            sysUserMapper.setPhone(userId, sysUser.getPhone());
        }
        if(sysUser.getEmail() != null){
            sysUserMapper.setEmail(userId, sysUser.getEmail());
        }
        if(sysUser.getDescription() != null){
            sysUserMapper.setDescription(userId, sysUser.getDescription());
        }
        redisService.updateToken(token);
    }

    @Override
    public void updatePasswd(String token, PasswdDto passwdDto) {
        SysUser sysUser = AuthContextUtil.get();
        String oldPasswd = DigestUtils.md5DigestAsHex(passwdDto.getOldPasswd().getBytes());
        if(!oldPasswd.equals(sysUser.getPassword())){
            throw new TomException(ResultCodeEnum.LOGIN_ERROR);
        }
        String newPasswd = DigestUtils.md5DigestAsHex(passwdDto.getNewPasswd().getBytes());
        sysUserMapper.setPasswd(sysUser.getId(), newPasswd);
        redisService.updateToken(token);
    }

    @Override
    public List<SysUser> getUserInfoByIds(List<Integer> ids) {
        return sysUserMapper.selectUsersByIds(ids);
    }


    @Autowired
    private MinioProperties minioProperties;
    @Override
    public void updateAvatar(String token, String avatarRequest) {
        try {
            // 创建MinioClient对象
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(minioProperties.getEndpointUrl())
                            .credentials(minioProperties.getAccessKey(),
                                    minioProperties.getSecreKey())
                            .build();

            // 创建bucket
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
                System.out.println("Bucket not exists.");
            }

            //获取上传文件名称
            // 1 每个上传文件名称唯一的   uuid生成 01.jpg
            //2 根据当前日期对上传文件进行分组 20230910
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = Calendar.getInstance(); // 获取当前日期
            String dateDir = sdf.format(calendar.getTime());
            // 20230910/u7r54209l097501.jpg
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            String filename = dateDir+"/"+uuid;

            String[] parts = avatarRequest.split(",");
            String contentType = parts[0].split(":")[1].split(";")[0];
//            System.out.println(contentType);
//            String base64Data = parts[1];
//            System.out.println(parts[1]);
            byte[] imageBytes = Base64.getDecoder().decode(parts[1]);
//            System.out.println("test"+avatarRequest);

            // 设置缓存头
            Map<String, String> headers = new HashMap<>();
            headers.put("Cache-Control", "max-age=86400");
            // 文件上传
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(minioProperties.getBucketName())
                            .object(filename)
                            .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                            .contentType(contentType)
                            .headers(headers)
                            .build());

            //获取上传文件在minio路径
            //http://127.0.0.1:9000/spzx-bucket/01.jpg
            String url = minioProperties.getEndpointUrl()+"/"+minioProperties.getBucketName()+"/"+filename;


            //更新数据库里的url
            SysUser sysUser = AuthContextUtil.get();
            sysUserMapper.setAvatar(sysUser.getId(), url);
            redisService.updateToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            throw new TomException(ResultCodeEnum.SYSTEM_ERROR);
        }
    }
}
