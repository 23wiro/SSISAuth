package com.ssis.ssisauth.data;
import net.minecraft.world.entity.player.Player;

public class AuthedPlayer {
    private String uuid;
    private String name;
    private String real_name;
    private String user_class;
    private String code;

    public AuthedPlayer(Player player, String real_name, String user_class, String code){
        this.uuid = player.getStringUUID();
        this.name = player.getName().getString();
        this.real_name = real_name;
        this.user_class = user_class;
        this.code = code;
    }

    public String getUuid(){
        return uuid;
    }

    public String getName(){
        return name;
    }

    public String getReal_name(){
        return real_name;
    }

    public Void setReal_name(String Real_name){this.real_name = Real_name; return null;}

    public String getUser_class(){
        return user_class;
    }

    public Void setUser_class(String User_class){this.real_name = User_class; return null;}

    public String getCode() {return code;}
}
