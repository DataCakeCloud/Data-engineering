package com.ushareit.dstask.constant;

/**
 * @author wuyan
 * @date 2021/08/06
 */
public enum SymbolEnum {
    /**
     * 半角符号
     */
    COMMA(",", "半角逗号"),
    PERIOD(".", "半角句号"),
    SEMICOLON(";", "半角分号"),
    COLON(":", "半角冒号"),
    DOUBLE_SEMICOLON(";;", "双半角分号"),
    APOSTROPHE("'", "半角单引号"),
    BREAK_LINE_SYMBOL("\n", "换行符"),
    UNDERLINE("_", "下划线"),
    DASH("-", "中划线"),
    STAR_SYMBOL("*", "星号"),
    EMPTY("", "空字符串"),
    QUESTION_MARK("?", "空字符串"),
    ;

    private String symbol;
    private String explanation;

    SymbolEnum(String symbol, String explanation) {
        this.symbol = symbol;
        this.explanation = explanation;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getExplanation() {
        return this.explanation;
    }
}
