package com.ssis.ssisauth.data;
import net.minecraft.world.entity.player.Player;
import com.google.gson.annotations.SerializedName;

public class AuthedPlayer {
    @SerializedName("uuid")
    private String uuid;

    @SerializedName("name")
    private String name;

    @SerializedName("real_name")
    private String real_name;

    @SerializedName("user_class")
    private String user_class;

    @SerializedName("code")
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

    public Void setUser_class(String User_class){this.user_class = User_class; return null;}

    public String getCode() {return code;}
}
