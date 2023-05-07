package top.ncserver.chatsync;


import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.extension.processor.AbstractMessageProcessor;
import org.smartboot.socket.extension.protocol.StringProtocol;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import top.ncserver.chatsync.V2.Until.MsgTool;

import java.io.IOException;
import java.util.logging.Logger;



public class Client extends BukkitRunnable {
    public static AioSession session;
    public static boolean isConnected=false;
    public final static Logger logger=Chatsync.getPlugin(Chatsync.class).getLogger();
    public static void connection(String host, int port) {
        try {
            AbstractMessageProcessor<String> processor = new AbstractMessageProcessor<String>(){
                @Override
                public void process0(AioSession aioSession, String msg) {
                    //logger.info(msg);
                    try {
                        if (msg.equals("heart message")){
                            //logger.info("heart message");
                            MsgTool.msgSend(session,"heart message");
                        }else
                            MsgTool.msgRead(session, msg);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                boolean clientmsg = Chatsync.config.getBoolean("clientmsg");
                @Override
                public void stateEvent0(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
                    if (stateMachineEnum.equals(StateMachineEnum.NEW_SESSION)){
                        isConnected=true;
                        logger.info("连接成功");
                        if (clientmsg) {
                        Object[] players = Chatsync.getPlugin(Chatsync.class).getServer().getOnlinePlayers().toArray();
                        for (Object player : players) {
                            ((Player) player).getPlayer().sendMessage("消息同步连接成功");
                        }
                        }
                    }else if (stateMachineEnum.equals(StateMachineEnum.SESSION_CLOSED)){
                        logger.warning("连接丢失");
                        isConnected = false;
                        if (!Chatsync.isOnDisable) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    while (!isConnected && Chatsync.getPlugin(Chatsync.class).isEnabled() && !Chatsync.isOnDisable) {
                                        try {
                                            Thread.sleep(5000);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        connection(Chatsync.config.getString("ip"), Chatsync.config.getInt("port"));
                                    }
                                    this.cancel();
                                }
                            }.runTaskAsynchronously(Chatsync.getPlugin(Chatsync.class));
                        }

                    }
                }
            };

            AioQuickClient client = new AioQuickClient(host, port, new StringProtocol(), processor);
            session = client.start();
        } catch (IOException ignored) {
        }

    }

    @Override
    public void run() {
        if (!Chatsync.isOnDisable) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    while (!isConnected && Chatsync.getPlugin(Chatsync.class).isEnabled() && !Chatsync.isOnDisable) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        connection(Chatsync.config.getString("ip"), Chatsync.config.getInt("port"));
                    }
                    this.cancel();
                }
            }.runTaskAsynchronously(Chatsync.getPlugin(Chatsync.class));
        }
        this.cancel();
    }
}
