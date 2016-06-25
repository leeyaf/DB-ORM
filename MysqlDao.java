package com.gsteam.common.util.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.gsteam.common.util.util.PropertiesUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mysql.jdbc.Statement;

/**
 * 1.0 使用HQL进行查询 提供类似ORM框架的实体保存更新删除功能 提供批量操作接口
 * 1.1 增加返回ResultSet的接口 增加新增返回插入的ID接口
 * 2.0 精简SqlHelper代码 删除persistence依赖 精简MysqlDao代码 使用原生SQL查询 更新删除实体返回影响行数
 * 2.1 加入c3p0连接池 加入配置文件
 * @author MikeD
 */
public class MysqlDao {
	// singleton
	private static final MysqlDao THIS=new MysqlDao();
	private MysqlDao(){}
	public static MysqlDao getInstances(){
		return THIS;
	}
	
	// connection operation
	private final String modulePackage="com.gsteam.common.util.module";
	private final ComboPooledDataSource dataSource=getDataSource();
	private ComboPooledDataSource getDataSource(){
		ComboPooledDataSource pooledDataSource=new ComboPooledDataSource();
		pooledDataSource.setUser("user");
		pooledDataSource.setPassword("password");
		pooledDataSource.setJdbcUrl("jdbcurl");
		try {
			pooledDataSource.setDriverClass("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		}
		pooledDataSource.setInitialPoolSize(3);
		pooledDataSource.setMinPoolSize(3);
		pooledDataSource.setMaxPoolSize(10);
		pooledDataSource.setMaxIdleTime(60);
		pooledDataSource.setMaxStatements(50);
		return pooledDataSource;
	}
	public Connection getConnection() throws Exception{
		return dataSource.getConnection();
	}
	public void rollback(Connection connection){
		try {
			DbUtils.rollback(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void close(Connection connection){
		try {
			DbUtils.close(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// base operation
	private final QueryRunner queryRunner=new QueryRunner();
	public int executeUpdate(String sql,List<?> params,Connection connection,boolean rowId,boolean close) throws Exception{
		try {
			PreparedStatement pstm=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			int index=1;
			for (Object object : params) {
				pstm.setObject(index++, object);
			}
			int effectCount=pstm.executeUpdate();
			if(rowId){
				ResultSet rs=pstm.getGeneratedKeys();
				if(rs.next()) return rs.getInt(1);
			}
			else return effectCount;
			
			return -1;
		} catch (Exception e) {
			throw e;
		} finally {
			if (close) close(connection);
		}
	}
	public <T> T executeQuery(String sql,List<?> params,ResultSetHandler<T> handler, Connection conn, boolean close) throws Exception{
		try {
			return queryRunner.query(conn, sql, handler, params.toArray());	
		} catch (Exception e) {
			throw e;
		} finally {
			if (close) close(conn);
		}
	}
	public ResultSet executeQuery(String sql,List<?> params,Connection connection, boolean close) throws Exception{
		try {
			PreparedStatement pstm=connection.prepareStatement(sql);
			int index=1;
			for (Object object : params) {
				pstm.setObject(index++, object);
			}
			return pstm.executeQuery();
		} catch (Exception e) {
			throw e;
		} finally {
			if (close) close(connection);
		}
	}
	public <T> int[] batch(String sql,List<List<?>> values,Connection conn, boolean close) throws Exception{
		int size=values.size();
		Object[][] objects=new Object[size][];
		int offset=0;
		for (List<?> list : values) {
			objects[offset++]=list.toArray();
		}
		try {
			return queryRunner.batch(conn, sql, objects);
		} catch (Exception e) {
			throw e;
		} finally {
			if (close) close(conn);
		}
	}

	// entity operation
	private final SqlHelper queryStringHelper=new SqlHelper(modulePackage);
	public <T> int save(T entity) throws Exception{
		SqlValue sv=queryStringHelper.createSaveSql(entity);
		Connection connection=getConnection();
		return executeUpdate(sv.getSql(), sv.getValues(), connection,true, true);
	}
	public <T> int save(T entity,Connection connection) throws Exception{
		SqlValue sv=queryStringHelper.createSaveSql(entity);
		return executeUpdate(sv.getSql(), sv.getValues(), connection,true, false);
	}
	
	public <T> int update(T entity) throws Exception{
		SqlValue sv=queryStringHelper.createUpdateSql(entity);
		Connection connection=getConnection();
		return executeUpdate(sv.getSql(), sv.getValues(), connection,false, true);
	}
	public <T> int update(T entity,Connection connection) throws Exception{
		SqlValue sv=queryStringHelper.createUpdateSql(entity);
		return executeUpdate(sv.getSql(), sv.getValues(), connection, false,false);
	}
	
	public <T> int delete(T entity) throws Exception{
		SqlValue sv=queryStringHelper.createDeleteSql(entity);
		Connection connection=getConnection();
		return executeUpdate(sv.getSql(), sv.getValues(), connection,false, true);
	}
	public <T> int delete(T entity, Connection connection) throws Exception{
		SqlValue sv=queryStringHelper.createDeleteSql(entity);
		return executeUpdate(sv.getSql(), sv.getValues(), connection,false, true);
	}
	
	// query operation
	private final CustomBasicRowProcessor rowProcessor=new CustomBasicRowProcessor();
	public <T> List<T> getList(String sql,List<?> params) throws Exception{
		Connection connection=getConnection();
		Class<T> entityClass=queryStringHelper.getClassFromSql(sql);
		return executeQuery(sql, params, new BeanListHandler<T>(entityClass, rowProcessor), connection, true);
	}
	public <T> List<T> getList(String sql,List<?> params,Connection connection) throws Exception{
		Class<T> entityClass=queryStringHelper.getClassFromSql(sql);
		return executeQuery(sql, params, new BeanListHandler<T>(entityClass, rowProcessor), connection, false);
	}
	
	public <T> T getOne(String sql,List<?> params) throws Exception{
		Class<T> entityClass=queryStringHelper.getClassFromSql(sql);
		Connection connection=getConnection();
		return executeQuery(sql, params, new BeanHandler<T>(entityClass, rowProcessor), connection, true);
	}
	public <T> T getOne(String sql,List<?> params,Connection connection) throws Exception{
		Class<T> entityClass=queryStringHelper.getClassFromSql(sql);
		return executeQuery(sql, params, new BeanHandler<T>(entityClass, rowProcessor), connection, false);
	}
	public <T> T getById(String sql,int id) throws Exception{
		Class<T> entityClass=queryStringHelper.getClassFromSql(sql);
		List<Object> params=new ArrayList<Object>();
		params.add(id);
		Connection connection=getConnection();
		return executeQuery(sql, params, new BeanHandler<T>(entityClass, rowProcessor), connection, true);
	}
	public <T> T getById(String sql,int id,Connection connection) throws Exception{
		Class<T> entityClass=queryStringHelper.getClassFromSql(sql);
		List<Object> params=new ArrayList<Object>();
		params.add(id);
		return executeQuery(sql, params, new BeanHandler<T>(entityClass, rowProcessor), connection, false);
	}
	public Long getLong(String sql,List<?> params) throws Exception{
		Connection connection=getConnection();
		return executeQuery(sql, params, new ScalarHandler<Long>(), connection, true);
	}
	public Long getLong(String sql,List<?> params,Connection connection) throws Exception{
		return executeQuery(sql, params, new ScalarHandler<Long>(), connection, false);
	}
}
