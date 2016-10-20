# DB-ORM

If you dont like the Hibernate or some else big bloated ORM frameworks, you should try this to write a new ORM framework of your own. :D

## Introduction

原理介绍文章参见[这里](http://www.jianshu.com/p/f52d34ae9289)

## Installation

下载源码，使用maven构建

## Example

使用之前你需要创建一个module

```java
public class User{
	@Id
	private Integer id;
	private String name;
	private Integer sex;	// 1 for male and 2 for female
	private Integer age;

	// getter and setter
}
```

### 新增

```java
public static void main(String[] args) {
	Dao dao=Dao.getInstances();
	User u=new User();
	u.setName("a");
	u.setSex(1);
	u.setAge(24);
	try {
		int retId=dao.save(u);
		// do something..
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

### 更新

```java
public static void main(String[] args) {
	Dao dao=Dao.getInstances();
	User u=new User();
	u.setId(123);
	u.setName("Mike");
	try {
		int effect=dao.update(u);
		// do something..
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

### 删除

```java
public static void main(String[] args) {
	Dao dao=Dao.getInstances();
	User u=new User();
	u.setId(123);
	try {
		int effect=dao.delete(u);
		// do something..
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

### 查询

```java
public static void main(String[] args) {
	Dao dao=Dao.getInstances();
	SqlQuery query=new SqlQuery();	// the Query object
	query.appendSql("select * from user where sex = ? ");
	query.addParam(1);
	try {
		List<User> users=dao.getList(query);
		// do something..
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

是不是很简单？

## Others

QQ交流群: 457364985
