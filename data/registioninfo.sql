/*
Navicat MySQL Data Transfer

Source Server         : MySQL
Source Server Version : 50015
Source Host           : localhost:3306
Source Database       : smallrecipedb

Target Server Type    : MYSQL
Target Server Version : 50015
File Encoding         : 65001

Date: 2016-02-28 23:28:58
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for registioninfo
-- ----------------------------
DROP TABLE IF EXISTS `registioninfo`;
CREATE TABLE `registioninfo` (
  `ID` int(20) NOT NULL auto_increment,
  `usernumber` varchar(30) NOT NULL default '',
  `username` varchar(200) default NULL,
  `sessionid` varchar(200) default NULL,
  `password` varchar(50) default NULL,
  `userid` varchar(20) NOT NULL default '',
  `userlogo` varchar(500) default NULL,
  `userlogourl` varchar(255) default NULL,
  PRIMARY KEY  (`ID`,`usernumber`,`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of registioninfo
-- ----------------------------
INSERT INTO `registioninfo` VALUES ('41', '15221340931', '系统管理员', '1456634130136', '123456', '50000', 'D:/androidInfo/apache-tomcat-6.0.29/webapps/SmallRecipeService/userlogo/15221340931.jpg', 'http://192.168.51.109:8080/SmallRecipeService/userlogo/15221340931.jpg');
INSERT INTO `registioninfo` VALUES ('42', '10086', '测试账户50034', '', '123456', '50034', 'D:/androidInfo/apache-tomcat-6.0.29/webapps/SmallRecipeService/userlogo/10086.jpg', 'http://192.168.51.109:8080/SmallRecipeService/userlogo/10086.jpg');
