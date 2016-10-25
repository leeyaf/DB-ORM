package org.leeyaf.dborm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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


/**
 * Core class of DB-ORM framework.
 * This is only one class you should to know, and use most. 
 * 
 * @author leeyaf
 */
public class Dao {
	// singleton
	private static final Dao THIS=new Dao();
	private Dao(){}
	public static Dao getInstances(){
		return THIS;
	}
	private static final String modulePackage=PropertiesUtil.getString("jdbc.module.package");
	protected static String getModulePackage() {
		return modulePackage;
	}
	private final AbstractSqlHelper sqlHelper=new MysqlSqlHelper();
	private final DataSource dataSource=getDataSource();
	
	// connection operation
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
	public static void rollback(Connection connection){
		try {
			DbUtils.rollback(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void close(Connection connection){
		try {
			DbUtils.close(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void close(PreparedStatement statement){
		try {
			DbUtils.close(statement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void close(ResultSet resultSet){
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
	public <T> T executeQuery(SqlQuery query,ResultSetHandler<T> handler, Connection conn, boolean close) throws SQLException{
		try {
			return queryRunner.query(conn, query.getSql(), handler, query.getParams());	
		} catch (SQLException e) {
			throw e;
		} finally {
			if (close) close(conn);
		}
	}
	public int executeUpdate(SqlQuery query,Connection connection,boolean rowId,boolean close) throws SQLException{
		PreparedStatement pstm=null;
		ResultSet rs=null;
		try {
			pstm=connection.prepareStatement(query.getSql(), Statement.RETURN_GENERATED_KEYS);
			int index=1;
			for (Object object : query.getParams()) {
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
	public <T> int save(T entity) throws Exception{
		SqlQuery query=sqlHelper.createSaveSql(entity);
		Connection connection=getConnection();
		return executeUpdate(query, connection, true, true);
	}
	public <T> int save(T entity,Connection connection) throws Exception{
		SqlQuery query=sqlHelper.createSaveSql(entity);
		return executeUpdate(query, connection, true, false);
	}
	
	public <T> int update(T entity) throws Exception{
		SqlQuery query=sqlHelper.createUpdateSql(entity);
		Connection connection=getConnection();
		return executeUpdate(query, connection, false, true);
	}
	public <T> int update(T entity,Connection connection) throws Exception{
		SqlQuery query=sqlHelper.createUpdateSql(entity);
		return executeUpdate(query, connection, false,false);
	}
	
	public <T> int delete(T entity) throws Exception{
		SqlQuery query=sqlHelper.createDeleteSql(entity);
		Connection connection=getConnection();
		return executeUpdate(query, connection,false, true);
	}
	public <T> int delete(T entity, Connection connection) throws Exception{
		SqlQuery query=sqlHelper.createDeleteSql(entity);
		return executeUpdate(query, connection,false, false);
	}
	
	// query operation
	private final CustomBasicRowProcessor rowProcessor=new CustomBasicRowProcessor();
	
	public <T> T getOne(SqlQuery query) throws Exception{
		Class<T> entityClass=sqlHelper.getClassFromSql(query.getSql());
		Connection connection=getConnection();
		return executeQuery(query, new BeanHandler<T>(entityClass, rowProcessor), connection, true);
	}
	public <T> T getOne(SqlQuery query,Connection connection) throws Exception{
		Class<T> entityClass=sqlHelper.getClassFromSql(query.getSql());
		return executeQuery(query, new BeanHandler<T>(entityClass, rowProcessor), connection, false);
	}
	public <T> T getOne(SqlQuery query,Class<T> handler) throws Exception{
		Connection connection=getConnection();
		return executeQuery(query, new BeanHandler<T>(handler, rowProcessor), connection, true);
	}
	public <T> T getOne(SqlQuery query,Class<T> handler,Connection connection) throws Exception{
		return executeQuery(query, new BeanHandler<T>(handler, rowProcessor), connection, false);
	}
	
	public <T> List<T> getList(SqlQuery query) throws Exception{
		Connection connection=getConnection();
		Class<T> entityClass=sqlHelper.getClassFromSql(query.getSql());
		return executeQuery(query, new BeanListHandler<T>(entityClass, rowProcessor), connection, true);
	}
	public <T> List<T> getList(SqlQuery query,Connection connection) throws Exception{
		Class<T> entityClass=sqlHelper.getClassFromSql(query.getSql());
		return executeQuery(query, new BeanListHandler<T>(entityClass, rowProcessor), connection, false);
	}
	public <T> List<T> getList(SqlQuery query,Class<T> handler) throws Exception{
		Connection connection=getConnection();
		return executeQuery(query, new BeanListHandler<T>(handler, rowProcessor), connection, true);
	}
	public <T> List<T> getList(SqlQuery query,Class<T> handler,Connection connection) throws Exception{
		return executeQuery(query, new BeanListHandler<T>(handler, rowProcessor), connection, false);
	}
	
	public <T> T getById(Class<T> clazz,Object id) throws Exception{
		SqlQuery query=sqlHelper.createFindByIdSql(clazz, id);
		Connection connection=getConnection();
		return executeQuery(query, new BeanHandler<T>(clazz, rowProcessor), connection, true);
	}
	public <T> T getById(Class<T> clazz,Object id,Connection connection) throws Exception{
		SqlQuery query=sqlHelper.createFindByIdSql(clazz, id);
		return executeQuery(query, new BeanHandler<T>(clazz, rowProcessor), connection, false);
	}
	
	public Long getLong(SqlQuery query) throws Exception{
		Connection connection=getConnection();
		return executeQuery(query, new ScalarHandler<Long>(), connection, true);
	}
	public Long getLong(SqlQuery query,Connection connection) throws Exception{
		return executeQuery(query, new ScalarHandler<Long>(), connection, false);
	}
	
	public Long getCount(SqlQuery query,Connection connection) throws Exception{
		SqlQuery countQuery=sqlHelper.getCountSql(query);
		return getLong(countQuery, connection);
	}
}
