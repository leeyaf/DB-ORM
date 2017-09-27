# DB-ORM

If you dont like the Hibernate or some else big bloated ORM frameworks, you should try this to write a new ORM framework of your own. :D

## Introduction

原理介绍文章参见[这里](http://www.jianshu.com/p/f52d34ae9289)

注意事项：

* 默认使用tomcat的链接池（其他连接池请自行build，或fork扩展）
* 默认事务自动提交（一个用例多次查询，请手动创建事务，避免数据库创建多个事务）
* 只对常用数据库操作进行伪ORM封装（特殊查询请使用jdbc）
* 无任何缓存

## Installation

下载源码，使用maven构建

## Example

使用之前你需要创建一个module，这里所有的字段都要使用封装类型，而不是直接类型。就是说int要用Integer代替，float要用Float代替。

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

## Automation

可以使用 <code>ModuleGenerator.generate("tabel_name");</code> 自动生成实体映射文件。
