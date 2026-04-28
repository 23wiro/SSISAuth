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

    public String getUser_class(){
        return user_class;
    }

    public String getCode() {return code;}
}
