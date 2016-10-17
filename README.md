# DB-ORM

If you dont like the Hibernate or some else big bloated ORM frameworks, you should try this to write a new ORM framework of your own. :D

## Introduction

原理介绍文章参见[这里](http://www.jianshu.com/p/f52d34ae9289)

## Installation

下载源码，使用maven构建即可，build出来是一个jar文件。

## Example

下面是一个简单的查询用户列表例子

```java
public static void main(String[] args) {
	MysqlDao dao=MysqlDao.getInstances();
	String sql="select * from user where sex = ? ";
	List<Object> params=new ArrayList<Object>();
	params.add(1);
	try {
		List<User> users=dao.getList(sql, params);
		for (User user : users) {
			System.out.println(user.getName());
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

新增用户到数据库

```java
public static void main(String[] args) {
	MysqlDao dao=MysqlDao.getInstances();
  User u=new User();
  u.setName("a");
  u.setSex("1");
  u.setAge("24");
	try {
    int retId=dao.save(u);
    System.out.println(retId);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```
