package org.leeyaf.dborm;

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
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.leeyaf.dborm.core.CustomBasicRowProcessor;

import com.mysql.jdbc.Statement;

/**
 * @author leeyaf
 */
public class MysqlDao {
	// singleton
	private static final MysqlDao THIS=new MysqlDao();
	private MysqlDao(){}
	public static MysqlDao getInstances(){
		return THIS;
	}
	
	// connection operation
	private final String modulePackage=PropertiesUtil.getString("jdbc.module.package");
	private final DataSource dataSource=getDataSource();
	private DataSource getDataSource(){
		PoolProperties p = new PoolProperties();
        p.setUrl(PropertiesUtil.getString("jdbc.url"));
        p.setDriverClassName("com.mysql.jdbc.Driver");
        p.setUsername(PropertiesUtil.getString("jdbc.username"));
        p.setPassword(PropertiesUtil.getString("jdbc.password"));
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setMaxActive(PropertiesUtil.getInt("jdbc.connection.max"));
        p.setInitialSize(PropertiesUtil.getInt("jdbc.connection.min"));
        p.setMaxIdle(PropertiesUtil.getInt("jdbc.connection.max"));
        p.setMinIdle(PropertiesUtil.getInt("jdbc.connection.min"));
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);
        return datasource;
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
	public void close(PreparedStatement statement){
		try {
			DbUtils.close(statement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void close(ResultSet resultSet){
		try {
			DbUtils.close(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void destory(){
		dataSource.close();
	}
	
	// base operation
	private final QueryRunner queryRunner=new QueryRunner();
	public <T> T executeQuery(String sql,List<?> params,ResultSetHandler<T> handler, Connection conn, boolean close) throws SQLException{
		try {
			return queryRunner.query(conn, sql, handler, params.toArray());	
		} catch (SQLException e) {
			throw e;
		} finally {
			if (close) close(conn);
		}
	}
	public ResultSet executeQuery(String sql,List<?> params,Connection connection, boolean close) throws SQLException{
		try {
			PreparedStatement pstm=connection.prepareStatement(sql);
			int index=1;
			for (Object object : params) {
				pstm.setObject(index++, object);
			}
			return pstm.executeQuery();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (close){
				// TODO: close ResultSet Statement
				close(connection);
			}
		}
	}
	public int executeUpdate(String sql,List<?> params,Connection connection,boolean rowId,boolean close) throws SQLException{
		PreparedStatement pstm=null;
		ResultSet rs=null;
		try {
			pstm=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			int index=1;
			for (Object object : params) {
				pstm.setObject(index++, object);
			}
			int effectCount=pstm.executeUpdate();
			if(rowId){
				rs=pstm.getGeneratedKeys();
				if(rs.next()) return rs.getInt(1);
			}
			else return effectCount;
			
			return -1;
		} catch (SQLException e) {
			throw e;
		} finally {
			if (close){
				close(rs);
				close(pstm);
				close(connection);
			}
		}
	}
	public <T> int[] batch(String sql,List<List<?>> values,Connection conn, boolean close) throws SQLException{
		int size=values.size();
		Object[][] objects=new Object[size][];
		int offset=0;
		for (List<?> list : values) {
			objects[offset++]=list.toArray();
		}
		try {
			return queryRunner.batch(conn, sql, objects);
		} catch (SQLException e) {
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
		return executeUpdate(sv.getSql(), sv.getValues(), connection,false, false);
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
	public <T> List<T> getList(String sql,List<?> params,Class<T> handler) throws Exception{
		Connection connection=getConnection();
		return executeQuery(sql, params, new BeanListHandler<T>(handler, rowProcessor), connection, true);
	}
	public <T> List<T> getList(String sql,List<?> params,Class<T> handler,Connection connection) throws Exception{
		return executeQuery(sql, params, new BeanListHandler<T>(handler, rowProcessor), connection, false);
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
	public <T> T getOne(String sql,List<?> params,Class<T> handler) throws Exception{
		Connection connection=getConnection();
		return executeQuery(sql, params, new BeanHandler<T>(handler, rowProcessor), connection, true);
	}
	public <T> T getOne(String sql,List<?> params,Class<T> handler,Connection connection) throws Exception{
		return executeQuery(sql, params, new BeanHandler<T>(handler, rowProcessor), connection, false);
	}
	
	public <T> T getById(String sql,Object id) throws Exception{
		Class<T> entityClass=queryStringHelper.getClassFromSql(sql);
		List<Object> params=new ArrayList<Object>();
		params.add(id);
		Connection connection=getConnection();
		return executeQuery(sql, params, new BeanHandler<T>(entityClass, rowProcessor), connection, true);
	}
	public <T> T getById(String sql,Object id,Connection connection) throws Exception{
		Class<T> entityClass=queryStringHelper.getClassFromSql(sql);
		List<Object> params=new ArrayList<Object>();
		params.add(id);
		return executeQuery(sql, params, new BeanHandler<T>(entityClass, rowProcessor), connection, false);
	}
	public <T> T getById(String sql,Object id,Class<T> handler) throws Exception{
		List<Object> params=new ArrayList<Object>();
		params.add(id);
		Connection connection=getConnection();
		return executeQuery(sql, params, new BeanHandler<T>(handler, rowProcessor), connection, true);
	}
	public <T> T getById(String sql,Object id,Class<T> handler,Connection connection) throws Exception{
		List<Object> params=new ArrayList<Object>();
		params.add(id);
		return executeQuery(sql, params, new BeanHandler<T>(handler, rowProcessor), connection, false);
	}
	
	public Long getLong(String sql,List<Object> params) throws Exception{
		Connection connection=getConnection();
		return executeQuery(sql, params, new ScalarHandler<Long>(), connection, true);
	}
	public Long getLong(String sql,List<Object> params,Connection connection) throws Exception{
		return executeQuery(sql, params, new ScalarHandler<Long>(), connection, false);
	}
	
	public Long getCount(String sql,List<Object> params,Connection connection) throws Exception{
		SqlValue sv=queryStringHelper.getCountSql(sql, params);
		return getLong(sv.getSql(), sv.getValues(), connection);
	}
}
