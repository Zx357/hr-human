package com.kadmin.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.mobile.domain.MobileChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MobileChatMessageMapper extends BaseMapper<MobileChatMessage> {

    /**
     * 按会话聚合未读消息数（单条 SQL 替代会话循环逐个 count 的 N+1）：
     * 群聊按 group_id 聚合，单聊按对方(发送方)员工聚合，
     * 已读水位取自 mobile_chat_read_state，无已读记录视为 0。
     */
    @Select("""
            <script>
            SELECT m.chat_type AS chatType,
                   CASE WHEN m.chat_type = 2 THEN m.from_employee_id ELSE m.group_id END AS targetId,
                   COUNT(*) AS unread
            FROM mobile_chat_message m
            LEFT JOIN mobile_chat_read_state r
              ON r.employee_id = #{employeeId}
             AND r.chat_type = m.chat_type
             AND r.target_id = CASE WHEN m.chat_type = 2 THEN m.from_employee_id ELSE m.group_id END
            WHERE m.from_employee_id != #{employeeId}
              AND (
                (m.chat_type = 2 AND m.peer_employee_id = #{employeeId})
                <if test="groupIds != null and groupIds.size() > 0">
                OR (m.chat_type = 1 AND m.group_id IN
                    <foreach collection='groupIds' item='gid' open='(' separator=',' close=')'>#{gid}</foreach>)
                </if>
              )
              AND m.id > IFNULL(r.last_read_message_id, 0)
            GROUP BY m.chat_type, CASE WHEN m.chat_type = 2 THEN m.from_employee_id ELSE m.group_id END
            </script>
            """)
    List<Map<String, Object>> countUnreadGroupByConversation(@Param("employeeId") Long employeeId,
            @Param("groupIds") List<Long> groupIds);
}
