# DB-ORM

If you dont like the Hibernate or some else big bloated ORM frameworks, you should try this to write a new ORM framework of your own. :D

## Introduction

原理介绍文章参见[这里](http://www.jianshu.com/p/f52d34ae9289)

## Installation

下载源码，使用maven构建

## Example

下面是一个简单的查询用户列表例子

```java
public static void main(String[] args) {
	Dao dao=Dao.getInstances();
	SqlQuery query=new SqlQuery();	// the Query object
	query.sqlAppend("select * from user where sex = ? ");
	query.paramAdd(1);	// 1 for male and 2 for female
	try {
		List<User> users=dao.getList(query);
		// do something..
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

新增用户到数据库

```java
public static void main(String[] args) {
	Dao dao=Dao.getInstances();
	User u=new User();
	u.setName("a");
	u.setSex("1");
	u.setAge("24");
	try {
		int retId=dao.save(u);
		// do something..
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

## Others

QQ交流群: 457364985
