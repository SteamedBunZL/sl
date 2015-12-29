package com.tuixin11sms.tx.model;

import java.io.Serializable;

/**
 * Server状态枚举
 * 
 * @author 郭伟洲
 * 
 */
public enum StatusCode implements Serializable {
	/**
	 * 成功
	 */
	RSP_OK,
	/**
	 * 操作失败
	 */
	OPT_FAILED,
	/** 非好友 */
	NOT_FRIEND,
	/** 修改语言成功 */
	CHANGE_LANG_SUCCESS,
	/** 修改语言不变 */
	CHANGE_LANG_NOTCHANGE,
	/** 修改语言失败 */
	CHANGE_LANG_FAILED,
	/** 修改地区成功 */
	CHANGE_AREA_SUCCESS,
	/** 修改地区失败 */
	CHANGE_AREA_FAILED,
	/** 修改地区不变 */
	CHANGE_AREA_NOTCHANGE,
	/**
	 * 修改职业成功
	 */
	CHANGE_JOB_SUCCESS,
	/**
	 * 修改职业不变
	 */
	CHANGE_JOB_NOTCHANGE,
	/**
	 * 修改职业失败
	 */
	CHANGE_JOB_FAILED,
	/**
	 * 修改职业不符合规则
	 */
	CHANGE_JOB_NOTRULE,
	/**
	 * 修改备注名不符合规则
	 */
	CHANGE_REMARK_NAME_NOTRULE,
	/**
	 * 修改爱好成功
	 */
	CHANGE_HOBBY_SUCCESS,
	/**
	 * 修改爱好不变
	 */
	CHANGE_HOBBY_NOTCHANGE,
	/**
	 * 修改爱好失败
	 */
	CHANGE_HOBBY_FAILED,
	/**
	 * 修改爱好不符合规则
	 */
	CHANGE_HOBBY_NOTRULE,
	/**
	 * 修改签名失败
	 */
	CHANGE_SIGN_FAILED,
	/**
	 * 修改签名成功
	 */
	CHANGE_SIGN_SUCCESS,
	/**
	 * 修改签名不符合规则
	 */
	CHANGE_SIGN_NOTRULE,
	/**
	 * 修改签名不变
	 */
	CHANGE_SIGN_NOTCHANGE,
	/**
	 * 修改昵称成功
	 */
	CHANGE_NAME_SUCCESS,
	/**
	 * 修改昵称失败
	 */
	CHANGE_NAME_FAILED,
	/**
	 * 修改昵称没改变
	 */
	CHANGE_NAME_NOTCHANGE,
	/**
	 * 账号不存在
	 */
	LOGIN_ACCOUNT_NO_EXIST,
	/**
	 * 用户名或密码错误
	 */
	LOGIN_NICK_PWD_ERROR,
	/**
	 * 昵称不符合规则
	 */
	LOGIN_NAME_FAIELD,
	/**
	 * 操作失败
	 */
	LOGIN_OPT_FAIELD,
	/**
	 * 昵称无效
	 */
	LOGIN_NICK_INVALID,
	/**
	 * 账号被封
	 */
	USER_BLOCK,
	/**
	 * 密码无效
	 */
	PWD_INVALID,
	/**
	 * 客户端版本不需更新
	 */
	VERSION_NO_UP,
	/**
	 * 系统消息，可能认识的人
	 */
	SYSTEM_MSG_MAYBE_KNOW,
	/**
	 * 服务器忙
	 */
	SERVER_BUSY,
	/**
	 * 系统消息，在其他地点登录
	 */
	SYSTEM_MSG_LOGIN_OTHER,
	/**
	 * 系统消息，系统对话框
	 */
	SYSTEM_MSG_SYS_DIALOG,
	/**
	 * 系统消息，手机绑定成功
	 */
	SYSTEM_MSG_PHONE_BINDED,
	/**
	 * 系统消息，邮箱绑定成功
	 */
	SYSTEM_MSG_EMAIL_BINDED,
	/**
	 * 系统消息，某某群组邀请你参加/你被某某群组删除了
	 */
	SYSTEM_MSG_GROUP_OPT_INFO,
	/**
	 * 系统消息，某某跟你打招呼
	 */
	SYSTEM_MSG_GREET,
	/**
	 * 系统消息，SNS好友推送
	 */
	SYSTEM_MSG_SNS_FRIEND,
	/**
	 * 系统消息，警告
	 */
	SYSTEM_MSG_SNS_WARN,
	/**
	 * 系统消息，加好友结果
	 */
	SYSTEM_MSG_JOIN_FRIEND,
	/**
	 * 系统消息，好友验证结果
	 */
	SYSTEM_MSG_VERIFY_FRIEND,
	/**
	 * 系统消息，你被某人加入黑名单或从黑名单移除
	 */
	SYSTEM_MSG_BLACK_LIST_OPT,
	/**
	 * 系统消息，你被禁言 或解除禁言
	 */
	SYSTEM_MSG_SHUTUP,
	/**
	 * 系统消息，你被警告
	 */
	SYSTEM_MSG_WARN,
	/**
	 * 系统消息，举报信息
	 */
	SYSTEM_MSG_REPORT,
	/**
	 * 系统消息 加群申请
	 */
	SYSTEM_MSG_REQUEST_GROUP,
	/**
	 * 设置管理员
	 */
	SYSTEM_MSG_SET_ADMIN,

	/**
	 * 此群不存在
	 */
	GROUP_NO_EXIST,
	/**
	 * 用户在黑名单中
	 */
	USER_IN_BLACK,
	/**
	 * 用户不存在
	 */
	USER_NO_EXIST,
	/**
	 * 用户不存在相册
	 */
	USERALBUM_NO_EXIST,
	/**
	 * 消息不存在
	 */
	MSG_NO_EXIST,
	/**
	 * 此群已解散
	 */
	GROUP_DISSOLVED,
	/**
	 * 群成员数量有误(用户列表小于1或大于199)
	 */
	GROUP_MEMBER_SIZE_INVALID,
	/**
	 * 操作群成员没有权限
	 */
	GROUP_MEMBER_OPT_NO_PERMISSION,
	/**
	 * 群成员数量超出限制
	 */
	GROUP_MEMBER_THAN_LIMIT,

	/**
	 * 群名称或简介包含特殊字符
	 */
	GROUP_NAME_INTRO_SPECIAL_CHAR,

	/**
	 * 群不存在
	 */
	GROUP_MODIFY_GROUP_NOT_EXIST,
	/**
	 * 群名称不符合规则
	 */
	GROUP_MODIFY_NAME_FAILED,
	/**
	 * 群简介不符合规范
	 */
	GROUP_MODIFY_INTRO_FAILED,
	/**
	 * 群头像不符合规范
	 */
	GROUP_MODIFY_AVATAR_FAILED,
	/**
	 * 群类型不符合规范
	 */
	GROUP_MODIFY_TYPE_FAILED,
	/**
	 * 群公告不符合规范
	 */
	GROUP_MODIFY_BULLENTIN_FAILED,
	/**
	 * 离开此群
	 */
	GROUP_LEAVE,
	/**
	 * 找不到用户
	 */
	FIND_NO_FRIEND,
	/**
	 * 手机号无效
	 */
	MOBILE_INVALID,
	/**
	 * 邮箱无效
	 */
	EMAIL_INVALID,
	/**
	 * 手机没有绑定
	 */
	MOBILE_NO_BINDED,
	/**
	 * 邮箱没有绑定
	 */
	EMAIL_NO_BINDED,
	/**
	 * 好友数量超出上限
	 */
	BUDDY_THAN_LIMIT,
	/**
	 * 拒绝好友请求
	 */
	REFUSE_FRIEND_REQ,
	/**
	 * 一定时间请求次数过多
	 */
	REQ_THAN_LIMIT,
	/**
	 * 手机号已绑定
	 */
	MOBILE_HAS_BINDED,
	/**
	 * email已绑定
	 */
	EMAIL_HAS_BINDED,
	/**
	 * 其他用户绑定了此手机号
	 */
	OTHER_BIND_THIS_MOBILE,
	/**
	 * 其他用户绑定此email
	 */
	OTHER_BIND_THIS_EMAIL,
	/**
	 * 本次上传数量过多
	 */
	UP_THE_NUMBER_THAN_LIMIT,
	/**
	 * 总数量过多
	 */
	THE_TOTAL_NUMBER_THAN_LIMIT,

	/**
	 * 申请成功，等待管理员同意
	 */
	GROUP_REQUEST_SUCCESS,
	/**
	 * 群已满员
	 */
	GROUP_FULL,
	/**
	 * 管理员已经到上限了
	 */
	ADMIN_FUll,
	/**
	 * 没有操作权限
	 */
	NO_PERMISSION, GROUP_OP_0_SUCCESS, GROUP_OP_1_SUCCESS,
	/**
	 * 禁言成功
	 */
	GROUP_ADD_SHUTUP_SUCCESS,
	/**
	 * 解除禁言成功
	 */
	GROUP_REMOVE_SHUTUP_SUCCESS,
	/**
	 * 在黑名单中
	 */
	GROUP_IN_BLACK_LIST,
	/**
	 * 黑名单过多
	 */
	GROUP_BLACK_LIST_TO_MORE,
	/**
	 * 群分页
	 */
	GROUP_FOR_PAGE,
	/**
	 * 不是群成员
	 */
	GROUP_NOT_MEMBER,

	/**
	 * 已经取完了
	 */
	GET_OVER,
	/**
	 * 黑名单设备
	 */
	THE_BLACK_DEVICE,

	/**
	 * 已经处理完了
	 */
	DONE,
	/**
	 * 封设备失败了
	 */
	BLOCK_FAILED,

	/**
	 * 注册次数过多--bobo
	 */
	MORE_REGIST,

	/**通知消息,瞬间被喜欢的提醒*/
	NOTICE_BLOG_LIKED,
	
	/**通知消息,瞬间被管理员删除*/
	BLOG_DELETE_BY_OP,
	
	/**举报瞬间返回，瞬间不存在*/
	BOLG_NO_EXIT;
}
