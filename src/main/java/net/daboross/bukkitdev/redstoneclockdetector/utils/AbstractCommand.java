package net.daboross.bukkitdev.redstoneclockdetector.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class AbstractCommand {

    protected String coloredUsage;
    protected String perm;
    protected AbstractCommand[] children;
    protected Token[] tokens;

    public AbstractCommand(String usage, String perm, AbstractCommand[] children) {
        this.perm = perm;
        this.children = children == null ? new AbstractCommand[0] : children;
        this.buildTokens(usage);
    }

    public void showUsage(CommandSender sender, String rootCommand) {
        if (sender.hasPermission(this.perm)) {
            sender.sendMessage(ChatColor.YELLOW + rootCommand + ChatColor.WHITE + " " + this.coloredUsage);
        }
        for (AbstractCommand child : this.children) {
            child.showUsage(sender, rootCommand);
        }
    }

    public boolean execute(CommandSender sender, String[] args) throws PermissionsException, UsageException {
        return this.execute(sender, args, new MatchResult[0]);
    }

    protected boolean execute(CommandSender sender, String[] args, MatchResult[] matchResults) throws PermissionsException, UsageException {
        // fill possible optional args with null.
        List<String> argList = new ArrayList<String>(Arrays.asList(args));
        for (int i = 0; i < this.tokens.length - args.length; ++i) {
            argList.add(null);
        }

        // continue to match args.
        List<MatchResult> matchResultList = new ArrayList<MatchResult>(Arrays.asList(matchResults));
        for (int i = matchResultList.size(); i < this.tokens.length; ++i) {
            MatchResult matchResult = this.tokens[i].match(argList.get(i));
            if (matchResult == null) {
                return false;
            }
            matchResultList.add(matchResult);
        }

        if (args.length > this.tokens.length) {
            // args is too long, try child commands.
            boolean matched = false;
            for (AbstractCommand child : this.children) {
                matched = matched || child.execute(sender, args, matchResultList.toArray(new MatchResult[0]));
            }
            return matched;
        } else {
            // ready to execute this command, check permission first.
            if (this.perm != null && !sender.hasPermission(this.perm)) {
                throw new PermissionsException(this.perm);
            }

            // get all data from args and execute command.
            List<MatchResult> dataList = new ArrayList<MatchResult>(args.length);
            for (MatchResult matchResult : matchResultList) {
                if (matchResult.hasData()) {
                    dataList.add(matchResult);
                }
            }
            return this.execute(sender, dataList.toArray(new MatchResult[0]));
        }
    }

    protected abstract boolean execute(CommandSender sender, MatchResult[] data) throws UsageException;

    private void buildTokens(String usage) {
        int splitPos = usage.indexOf("  ");
        String tokenDefine;
        if (splitPos == -1) {
            tokenDefine = usage;
            this.coloredUsage = tokenDefine;
        } else {
            tokenDefine = usage.substring(0, splitPos);
            this.coloredUsage = tokenDefine + ChatColor.GRAY + usage.substring(splitPos);
        }
        String[] tokenStrings = tokenDefine.split(" ");
        List<Token> tokenList = new ArrayList<Token>(tokenStrings.length);
        for (String tokenString : tokenStrings) {
            if (tokenString.isEmpty()) {
                continue;
            }
            tokenList.add(new Token(tokenString));
        }
        this.tokens = tokenList.toArray(new Token[0]);

        for (AbstractCommand child : children) {
            if (child.tokens.length < this.tokens.length) {
                throw new IllegalArgumentException("Child command is shorter than parent command.");
            }
            for (int i = 0; i < this.tokens.length; ++i) {
                if (!this.tokens[i].equals(child.tokens[i])) {
                    throw new IllegalArgumentException("Child command does not have a prefix of parent command.");
                }
            }
        }
    }

    @EqualsAndHashCode(exclude = {"optional"})
    protected static class Token {

        private final boolean optional;
        private final String template;

        public Token(String template) {
            if (template.length() >= 2) {
                if (template.charAt(0) == '[' && template.charAt(template.length() - 1) == ']') {
                    this.template = null;
                    this.optional = true;
                    return;
                } else if (template.charAt(0) == '<' && template.charAt(template.length() - 1) == '>') {
                    this.template = null;
                    this.optional = false;
                    return;
                }
            }
            this.template = template;
            this.optional = false;
        }

        public MatchResult match(String part) {
            if (part == null) {
                return this.optional ? new MatchResult() : null;
            }
            if (this.template == null) {
                return new MatchResult(part);
            } else {
                return this.template.equalsIgnoreCase(part) ? new MatchResult() : null;
            }
        }

    }

    @AllArgsConstructor
    protected static class MatchResult {

        private final String data;

        public MatchResult() {
            this.data = null;
        }

        public boolean hasData() {
            return this.data != null;
        }

        public Integer getInteger() {
            if (this.data == null) {
                return null;
            }
            Integer result;
            try {
                result = Integer.parseInt(this.data);
            } catch (NumberFormatException e) {
                return null;
            }
            return result;
        }

        public Double getDouble() {
            if (this.data == null) {
                return null;
            }
            Double result;
            try {
                result = Double.parseDouble(this.data);
            } catch (NumberFormatException e) {
                return null;
            }
            return result;
        }

        public String getString() {
            return this.data;
        }
    }
}
