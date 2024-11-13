# 数据初始化

-- 切换库
use my_db;

-- 用户表初始数据
INSERT INTO user (id, userAccount, userPassword, unionId, mpOpenId, userName, userAvatar, userProfile, userRole,
                  createTime, updateTime, isDelete)
VALUES (1, 'aixming', 'a1c021d43c899914ea835c3115261414', null, null, 'AixMing',
        'https://k.sinaimg.cn/n/sinakd20110/560/w1080h1080/20230930/915d-f3d7b580c33632b191e19afa0a858d31.jpg/w700d1q75cms.jpg',
        '一名无敌年轻有为的全栈工程师', 'admin', '2024-05-09 11:13:13', '2024-05-09 15:07:48', 0);