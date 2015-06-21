package fzn.projects.networkstatistics;

/**
 * Created by FzN on 15/6/7.
 * 常量类
 * 管理应用内交互所使用的常量
 */
public final class Constants {
    public static final char[] TIME_UNIT = { 'm', 'd', 'w', 'y' }; // 时间单位
    public static final int RULE_ADD_REQUEST = 0;
    public static final int RULE_EDIT_REQUEST = 1;
    public final class Intent {
        public static final String KEY_INTENT_SPEED_VALUE = "fzn.projects.networkstatistics.KEY_INTENT_SPEED_VALUE";
    }
    public final class Extra {
        public static final String SPEED = "SPEED";
        public static final String COMBOID = "COMBOID";
    }
}
