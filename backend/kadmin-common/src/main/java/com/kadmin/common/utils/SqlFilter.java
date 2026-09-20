package com.kadmin.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态 SQL 过滤条件：以占位符方式拼接 WHERE 片段，参数顺序与片段一致，杜绝 SQL 注入
 */
public final class SqlFilter {

    private final StringBuilder sql = new StringBuilder(" WHERE 1=1");
    private final List<Object> params = new ArrayList<>();

    public static SqlFilter create() {
        return new SqlFilter();
    }

    /**
     * 追加条件片段（片段内使用 ? 占位符，参数随后按序传入）
     */
    public SqlFilter append(String fragment, Object... fragmentParams) {
        sql.append(fragment);
        for (Object param : fragmentParams) {
            params.add(param);
        }
        return this;
    }

    public String getSql() {
        return sql.toString();
    }

    public List<Object> getParams() {
        return params;
    }

    /**
     * 复制一份，供在同一过滤条件上追加不同统计条件的查询使用
     */
    public SqlFilter copy() {
        SqlFilter clone = new SqlFilter();
        clone.sql.setLength(0);
        clone.sql.append(this.sql);
        clone.params.addAll(this.params);
        return clone;
    }
}
