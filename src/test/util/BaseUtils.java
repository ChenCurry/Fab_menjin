package test.util;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 数据库连接工具类
 * @author yu.chen
 *
 */
public class BaseUtils {

	//初始化c3p0
    private static DataSource dataSource=null;
    
    static{
    	//自动加载src目录下面的c3p0的配置文件，【c3p0-config.xml】
        dataSource = new ComboPooledDataSource();
    }
    
    public static QueryRunner getQueryRunner(){
        //第一步：创建QueryRunner对象，传入连接池对象
        //在创建QueryRunner对象的时候，如果传入数据对象dataSource，
        //那么在使用QueryRunner对象的方法时候，就不需要传入连接对象
        QueryRunner query=new QueryRunner(dataSource);
        //第二步：会自动从数据源中获取连接(不用关闭连接)
        return query;
    }
    
    /***
     * 实现增删改的公共方法
     * @param sql
     * @param arr
     * @return
     */
    public static boolean addUpdateDelete(String sql,Object[] arr){
        QueryRunner qr=getQueryRunner();
        int count;
        try {
            count = qr.update(sql, arr);
            if(count>0){
                return true;
            }else{
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
