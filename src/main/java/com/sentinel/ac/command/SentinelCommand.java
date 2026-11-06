package com.sentinel.ac.command;

import com.sentinel.ac.SentinelAC;
import com.sentinel.ac.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SentinelCommand implements CommandExecutor, TabCompleter {

    private final SentinelAC plugin;

    public SentinelCommand(SentinelAC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sentinel.admin")) {
            sender.sendMessage("§cSem permissão.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§7Uso: §f/" + label + " <alerts|reload|vl <jogador>>");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "alerts" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("§cApenas jogadores.");
                    return true;
                }
                PlayerData data = plugin.players().get(p.getUniqueId());
                if (data == null) {
                    return true;
                }
                data.alerts = !data.alerts;
                sender.sendMessage("§8[§cSentinel§8] §7Alertas: " + (data.alerts ? "§aligados" : "§cdesligados"));
            }
            case "reload" -> {
                plugin.reload();
                sender.sendMessage("§8[§cSentinel§8] §7Configuração recarregada.");
            }
            case "vl" -> {
                if (args.length < 2) {
                    sender.sendMessage("§7Uso: §f/" + label + " vl <jogador>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                PlayerData data = target == null ? null : plugin.players().get(target.getUniqueId());
                if (data == null) {
                    sender.sendMessage("§cJogador não encontrado.");
                    return true;
                }
                Map<String, Double> vl = data.vlSnapshot();
                if (vl.isEmpty()) {
                    sender.sendMessage("§8[§cSentinel§8] §f" + target.getName() + " §7não tem violações.");
                } else {
                    sender.sendMessage("§8[§cSentinel§8] §7Violações de §f" + target.getName() + "§7:");
                    vl.forEach((k, v) -> sender.sendMessage(String.format(Locale.ROOT, " §8- §c%s §7%.1f", k, v)));
                }
            }
            default -> sender.sendMessage("§7Uso: §f/" + label + " <alerts|reload|vl <jogador>>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission("sentinel.admin")) {
            return out;
        }
        if (args.length == 1) {
            for (String s : List.of("alerts", "reload", "vl")) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(s);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("vl")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    out.add(p.getName());
                }
            }
        }
        return out;
    }
}
