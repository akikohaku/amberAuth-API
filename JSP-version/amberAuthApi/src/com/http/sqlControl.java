package com.http;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.*;

public class sqlControl {
    private Connection connection=null;
    private Statement statement;
    private ResultSet resultSet;
    private String dbFilePath="";  //调试
    private String port="3306";
    private String name="";
    private String password="";
    private String database="amberauth";
    private String secret="amberwork";


    public sqlControl() throws ClassNotFoundException, SQLException {
        connection = Connection(dbFilePath);
    }
    public int register(JSONObject userinfo){

        String username=userinfo.getString("username");
        String password=userinfo.getString("password");
        String phone=userinfo.getString("phone");
        String email=userinfo.getString("email");

        if(!checkUsername(username)){
            return 1;
        }
        if(!checkPhone(phone)){
            return 2;
        }
        if(!checkEmail(email)){
            return 3;
        }
        String uuid=setUid();
        String todo="insert into users values(\""+username+"\",\""+uuid+"\",\""+password+"\")";
        // System.out.println(todo);
        try{
            int c=executeUpdate(todo);
            todo="insert into profile values(\""+uuid+"\",\""+phone+"\",\""+email+"\")";
            c+=executeUpdate(todo);
            Date time = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String current = sdf.format(time);
            todo="insert into token values(\""+uuid+"\",\""+new token().getToken(uuid,7200)+"\",7200,\""+current+"\")";
            c+=executeUpdate(todo);
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())+" [server]:数据库修改"+c+"行");
            return 0;
        }catch(Exception e){
            e.printStackTrace();
            destroyed();
            return -1;
        }
    }

    public JSONObject loginByUserName(JSONObject userinfo){
        String username=userinfo.getString("username");
        String password=userinfo.getString("password");
        if(tryLoginUserName(username,password)){
            String uuid=getUUID(username);
            String token=updateToken(uuid);

            JSONObject out = new JSONObject();
            try {
                //添加
                out.put("status",0);
                out.put("uuid",uuid);
                out.put("token",token);
                out.put("time",7200);
                return out;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if(tryLoginEmail(username,password)){
            String uuid=getUUIDByEmail(username);
            String token=updateToken(uuid);

            JSONObject out = new JSONObject();
            try {
                //添加
                out.put("status",0);
                out.put("uuid",uuid);
                out.put("token",token);
                out.put("time",7200);
                return out;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //错误
        JSONObject out = new JSONObject();
        try {
            //添加
            out.put("status",1);
            return out;
        } catch (JSONException e) {
            return null;
        }
    }

    public JSONObject sendEmail(JSONObject userinfo){
        String email=userinfo.getString("email");
        int ram=(int)(Math.random()*900000+100000);
        String code=String.valueOf(ram);
        try {
            new Mail().sendMail(email,code);
            JSONObject out = new JSONObject();
            String todo="insert into mail_ver values(\""+email+"\",\""+code+"\",NOW())";
            // System.out.println(todo);
            try{
                int c=executeUpdate(todo);
            }catch(Exception e){
                e.printStackTrace();
                destroyed();
            }
            try {
                //添加
                out.put("status",0);
                return out;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject out = new JSONObject();
        try {
            //添加
            out.put("status",1);
            return out;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public JSONObject sendSMS(JSONObject userinfo){
        String phone=userinfo.getString("phone");
        int ram=(int)(Math.random()*900000+100000);
        String code=String.valueOf(ram);
        try {
            new SMS().sendCode(phone,code,phone);
            JSONObject out = new JSONObject();
            String todo="insert into phone_ver values(\""+phone+"\",\""+code+"\",NOW())";
            // System.out.println(todo);
            try{
                int c=executeUpdate(todo);
            }catch(Exception e){
                e.printStackTrace();
                destroyed();
            }
            try {
                //添加
                out.put("status",0);
                return out;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject out = new JSONObject();
        try {
            //添加
            out.put("status",1);
            return out;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject loginByEmail(JSONObject userinfo){
        String email=userinfo.getString("email");
        String code=userinfo.getString("code");
        if(checkCode(email,code)){
            String todo="delete from mail_ver where email='"+email+"'";
            // System.out.println(todo);
            try{
                int c=executeUpdate(todo);
            }catch(Exception e){
                e.printStackTrace();
                destroyed();
            }
            todo="select * from profile where email='"+email+"'";
            try {
                ResultSet res = connection.createStatement().executeQuery(todo);
                if(res.next()) {
                    JSONObject out = new JSONObject();
                    try {
                        //添加
                        out.put("status",0);
                        out.put("uuid",res.getString("uuid"));
                        out.put("token",updateToken(res.getString("uuid")));
                        out.put("time",7200);
                        return out;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                destroyed();
            }
            return null;
        }else{
            JSONObject out = new JSONObject();
            try {
                //添加
                out.put("status",1);
                return out;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public JSONObject loginBySMS(JSONObject userinfo){
        String phone=userinfo.getString("phone");
        String code=userinfo.getString("code");
        if(checkCodeSMS(phone,code)){
            String todo="delete from phone_ver where phone='"+phone+"'";
            // System.out.println(todo);
            try{
                int c=executeUpdate(todo);
            }catch(Exception e){
                e.printStackTrace();
                destroyed();
            }
            todo="select * from profile where phone='"+phone+"'";
            try {
                ResultSet res = connection.createStatement().executeQuery(todo);
                if(res.next()) {
                    JSONObject out = new JSONObject();
                    try {
                        //添加
                        out.put("status",0);
                        out.put("uuid",res.getString("uuid"));
                        out.put("token",updateToken(res.getString("uuid")));
                        out.put("time",7200);
                        return out;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                destroyed();
            }
            return null;
        }else{
            JSONObject out = new JSONObject();
            try {
                //添加
                out.put("status",1);
                return out;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public JSONObject getProfile(JSONObject userinfo){
        String uuid=userinfo.getString("uuid");
        String token=userinfo.getString("token");
        String target=userinfo.getString("target");
        if(checkToken(uuid,token)){
            String todo="select * from users as a,profile as b where a.uuid=b.uuid AND a.uuid='"+target+"'";
            try {
                ResultSet res = connection.createStatement().executeQuery(todo);
                if(res.next()) {
                    JSONObject out = new JSONObject();
                    try {
                        //添加
                        out.put("status",0);
                        out.put("uuid",res.getString("uuid"));
                        out.put("username",res.getString("username"));
                        out.put("phone",res.getString("phone"));
                        out.put("email",res.getString("email"));
                        return out;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                destroyed();
            }
            return null;
        }
        JSONObject out = new JSONObject();
        try {
            //添加
            out.put("status",1);
            return out;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject saveProfile(JSONObject userinfo){
        String uuid=userinfo.getString("uuid");
        String token=userinfo.getString("token");
        String phone=userinfo.getString("phone");
        String email=userinfo.getString("email");
        if(checkToken(uuid,token)){
            String todo="update profile set phone='"+phone+"',email='"+email+"' where uuid='"+uuid+"'";
            try{
                int c=executeUpdate(todo);
                if(c>=1){
                    JSONObject out = new JSONObject();
                    try {
                        //添加
                        out.put("status",0);
                        return out;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                destroyed();
            }
        }
        JSONObject out = new JSONObject();
        try {
            //添加
            out.put("status",1);
            return out;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }
    public JSONObject savePass(JSONObject userinfo) {
        String uuid = userinfo.getString("uuid");
        String token = userinfo.getString("token");
        String pass = userinfo.getString("pass");
        String newpass = userinfo.getString("newpass");
        if(checkToken(uuid,token)){
            if(tryLoginUuid(uuid,pass)){
                String todo="update users set password='"+newpass+"' where uuid='"+uuid+"'";
                try{
                    int c=executeUpdate(todo);
                    if(c>=1){
                        JSONObject out = new JSONObject();
                        try {
                            //添加
                            out.put("status",0);
                            return out;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    destroyed();
                }
            }else{
                JSONObject out = new JSONObject();
                try {
                    //添加
                    out.put("status",1);
                    return out;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        JSONObject out = new JSONObject();
        try {
            //添加
            out.put("status",2);
            return out;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getAllUsers(JSONObject userinfo) {
        String uuid = userinfo.getString("uuid");
        String token = userinfo.getString("token");
        if(checkToken(uuid,token)){
            if(checkAdmin(uuid)){
                String todo="select * from users as a,profile as b where a.uuid=b.uuid";
                try {
                    ResultSet res = connection.createStatement().executeQuery(todo);
                    JSONObject out = new JSONObject();
                    out.put("status",0);
                    List<JSONObject> temp=new ArrayList<JSONObject>();
                    while(res.next()) {
                        JSONObject t = new JSONObject();
                        t.put("uuid",res.getString("uuid"));
                        t.put("username",res.getString("username"));
                        t.put("phone",res.getString("phone"));
                        t.put("email",res.getString("email"));
                        temp.add(t);
                    }
                    out.put("users",temp);
                    return out;
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    destroyed();
                }
            }else{
                JSONObject out = new JSONObject();
                out.put("status",1);
                return out;
            }
        }else{
            JSONObject out = new JSONObject();
            out.put("status",2);
            return out;
        }
        JSONObject out = new JSONObject();
        out.put("status",3);
        return out;
    }
    public JSONObject deleteUser(JSONObject userinfo) {
        String uuid = userinfo.getString("uuid");
        String token = userinfo.getString("token");
        String target=userinfo.getString("target");
        if(checkToken(uuid,token)){
            if(checkAdmin(uuid)){
                if(!checkAdmin(target)){


                String todo="delete from token where uuid='"+target+"'";
                try{
                    int c=executeUpdate(todo);
                    todo="delete from profile where uuid='"+target+"'";
                    c+=executeUpdate(todo);
                    todo="delete from users where uuid='"+target+"'";
                    c+=executeUpdate(todo);
                    if(c>0){
                        JSONObject out = new JSONObject();
                        out.put("status",0);
                        return out;
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    destroyed();
                }
                }else{
                    JSONObject out = new JSONObject();
                    out.put("status",4);
                    return out;
                }
            }else{
                JSONObject out = new JSONObject();
                out.put("status",1);
                return out;
            }
        }else{
            JSONObject out = new JSONObject();
            out.put("status",2);
            return out;
        }
        JSONObject out = new JSONObject();
        out.put("status",3);
        return out;
    }
    public JSONObject addUser(JSONObject userinfo) {
        String uuid = userinfo.getString("uuid");
        String token = userinfo.getString("token");
        String username=userinfo.getString("username");
        String phone=userinfo.getString("phone");
        String email=userinfo.getString("email");
        String pass=userinfo.getString("pass");

        if(checkToken(uuid,token)){
            if(checkAdmin(uuid)){
                if(!checkUsername(username)){
                    JSONObject out = new JSONObject();
                    out.put("status",5);
                    return out;
                }
                if(!checkPhone(phone)){
                    JSONObject out = new JSONObject();
                    out.put("status",6);
                    return out;
                }
                if(!checkEmail(email)){
                    JSONObject out = new JSONObject();
                    out.put("status",7);
                    return out;
                }
                String nuuid=setUid();
                String todo="insert into users values(\""+username+"\",\""+nuuid+"\",\""+password+"\")";
                // System.out.println(todo);
                try{
                    int c=executeUpdate(todo);
                    todo="insert into profile values(\""+nuuid+"\",\""+phone+"\",\""+email+"\")";
                    c+=executeUpdate(todo);
                    Date time = new Date(System.currentTimeMillis());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String current = sdf.format(time);
                    todo="insert into token values(\""+nuuid+"\",\""+new token().getToken(uuid,7200)+"\",7200,\""+current+"\")";
                    c+=executeUpdate(todo);
                    System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())+" [server]:数据库修改"+c+"行");
                    JSONObject out = new JSONObject();
                    out.put("status",0);
                    return out;
                }catch(Exception e){
                    e.printStackTrace();
                    destroyed();
                }
            }else{
                JSONObject out = new JSONObject();
                out.put("status",1);
                return out;
            }
        }else{
            JSONObject out = new JSONObject();
            out.put("status",2);
            return out;
        }
        JSONObject out = new JSONObject();
        out.put("status",3);
        return out;
    }
    private boolean checkCodeSMS(String phone,String code){
        String todo="select count(*) from phone_ver as a where a.phone='"+phone+"' AND a.code='"+code+"' AND timestampdiff(SECOND,a.time,NOW())<=1800";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getInt(1) == 1) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }
    private boolean checkCode(String email,String code){
        String todo="select count(*) from mail_ver as a where a.email='"+email+"' AND a.code='"+code+"' AND timestampdiff(SECOND,a.time,NOW())<=1800";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getInt(1) == 1) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }
    private boolean checkToken(String uuid,String token){
        String todo="select count(*) from token where uuid='"+uuid+"' AND token='"+token+"' AND timestampdiff(SECOND,time,NOW())<=7200";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getInt(1) == 1) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }

    private boolean checkAdmin(String uuid){
        String todo="select count(*) from admin where uuid='"+uuid+"'";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getInt(1) == 1) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }
    private boolean tryLoginUserName(String username,String password){
        String todo="select * from users where username='"+username+"'";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getString("password").equals(password)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }
    private boolean tryLoginUuid(String uuid,String password){
        String todo="select * from users where uuid='"+uuid+"'";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getString("password").equals(password)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }
    private  boolean tryLoginEmail(String email,String password){
        String todo="select * from profile as a,users as b where a.email='"+email+"' AND a.uuid=b.uuid";
        try{
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getString("password").equals(password)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }
    private String updateToken(String uuid){
        String token=new token().getToken(uuid,7200);
        Date time = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current = sdf.format(time);
        String todo="update token set token='"+token+"',time='"+current+"' where uuid='"+uuid+"'";
        try{
            int c=executeUpdate(todo);
        }catch(Exception e){
            e.printStackTrace();
            destroyed();
        }
        return token;
    }
    private String getUUID(String username){
        String todo="select * from users where username='"+username+"'";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                return res.getString("uuid");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return null;
    }
    private String getUUIDByEmail(String email){
        String todo="select * from profile where email='"+email+"'";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                return res.getString("uuid");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return null;
    }
    private boolean checkUsername(String username){
        String todo="select count(*) from users where username='"+username+"'";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getInt(1) == 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }
    private boolean checkPhone(String phone){
        String todo="select count(*) from profile where phone='"+phone+"'";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getInt(1) == 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }
    private boolean checkEmail(String email){
        String todo="select count(*) from profile where email='"+email+"'";
        try {
            ResultSet res = connection.createStatement().executeQuery(todo);
            if(res.next()) {
                if (res.getInt(1) == 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            destroyed();
        }
        return false;
    }
    public String setUid(){
        String temp="";
        boolean iscon=true;
        while(iscon){
            int ram=(int)(Math.random()*9000000+1000000);
            String t=String.valueOf(ram);
            String todo="select count(*) from users where uuid='"+t+"'";
            try {
                ResultSet res = connection.createStatement().executeQuery(todo);
                if(res.next()) {
                    if (res.getInt(1) == 0) {
                        temp = t;
                        iscon = false;
                    }
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                destroyed();
            }
        }
        return temp;
    }
    //建立连接
    public Connection Connection(String dbFilePath) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://"+dbFilePath+":"+port+"/"+database,name,password);
        System.out.println(conn);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())+" [server]:数据库已连接");
        return conn;
    }
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        if (null == connection) connection = Connection(dbFilePath);
        return connection;
    }

    private Statement getStatement() throws SQLException, ClassNotFoundException {
        if (null == statement) statement = getConnection().createStatement();
        return statement;
    }

    public int executeUpdate(String sql) throws SQLException, ClassNotFoundException {
        try {
            int c = getStatement().executeUpdate(sql);
            return c;
        } finally {
            //destroyed();
        }

    }
    public void destroyed() {
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())+" [server]:数据库关闭");
        try {
            if (null != connection) {
                connection.close();
                connection = null;
            }

            if (null != statement) {
                statement.close();
                statement = null;
            }

            if (null != resultSet) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())+" [server]:数据库关闭异常");
        }
        try{
            connection = getConnection();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
