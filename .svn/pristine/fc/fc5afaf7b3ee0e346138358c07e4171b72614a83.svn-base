 summary:
 本工程目的修改上传下载文件的功能，去除socketClient、AsynchrImageLoader和ImageLoader等相关的引用。
 修改上传下载文件为队列，所有功能均在FileTransfer中实现
 
 2013.08.20
 1、修改LoginSessionMagnager单例获取，context直接调用context.getApplicationContext(),避免activity引用被长期持有---顾问修改
 2、FileTransfer中的MTTaskBase添加mbTaskValid变量，判定任务是否有效，如果无效，在任务队列的addTask时就不添加。
	解决问题：有的下载任务URL非法，用“:”分割时不是四段。下载会异常。--顾问修改
 3、把GroupMsgRoom中使用mAsynloader.post(new Runnable() {}); 异步加载图片取代了之前的Utils.executorService.submit(Runnable)的方式，
	SingleMsgRoom中还未修改。--顾问修改

2013.08.21
 1、①去掉了Utils方法中SocketClient的引用，
	②修改了MessageActivity用户头像、
	③修改用户相册大图加载尺寸错误的问题。
	④修改用户相册大小图缓存判定地址和下载地址不一致导致每次加载都去下载的bug.
	⑤修改长短录音时长不显示的问题。因为把录音时长的毫秒值重复除以了1000  
	⑥修改长短录音到达时长时自动结束崩溃的bug,因为子线程不能操作popupWindow---shc
 2、为了解决地址url错误导致调用listener的exception方法时，传递的obj为null的问题，在创建任务后就进行任务是否有效的检查，在addTask之前检查。
 2013.08.21
 	1.修改GroupMsgRoom，BaseMsgRoom, SingleMsgRoom支持：
 		1).支持单行更新
 		2).解决第一次下拉加载更多时跳到底部的BUG
 		3).将异步加载处理放到了BaseMsgRoom中
 		4).对SingleMsgRoom进行了上述修改
 	2.修改了FileTransfer, 改变了非法任务的回调处理方式。
 2013.08.22
  1、不在下载task构造时调用任务listener的onErr方法，在调用者生成下载任务后，再去判定任务是否合法，如果不合法则去调用listener的onErr方法

 2013.08.23
    更新GroupMsgRoom.readHeadImg,解决同一用户头像多次下载的问题  
    更新FileTransfer，修改任务返回值，解决某些情况下没有断网直接重试的问题
现存的问题：
	GroupIndexAdapter中群组头像的处理流程不对，会闪动
2013.08.26
 1.聊天室内语音背景图。
 2013.08.27
 1.微博登陆会跳www.shenliao.com官网回调页面，目前原因是WeiboDialog类中的WeiboWebViewClient类shouldOverrideUrlLoading方法中
 getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));这句引起的，这句会通过浏览器跳到shenliao.com，现改成
 view.loadUrl(url);通过自己的webview来加载url。
 2.优化清除缓存文件，在列举文件时直接用new FilenameFilter()过滤删除。
 3、增加断点续传图片功能
 4、增加断点下载图片功能
 
  2013.08.27
 1、删除了DownloadAlbum、ImageLoader、AsyncImageLoader、DownloadImageTask、FileClient、NetTask、SocketReceiver、SocketClient、DataWrapper等类
 2、①增加下载大图进度显示
  	②增加录音结束且录音正在上传时界面有录音消息显示
	③录音时长一秒钟以下不发送消息
  
 2013.08.28
  1. 删除GUITaskQueue，ReadImageTask，GUITask，TaskQueue  
  2. 修改scanFile
  3、修改音频消息显示策略，刚发出消息时不显示时长，延时1s后录音结束时再设置时长显示
  4、修改AlbumItem为实现parcelable接口，否则会不可报序列化异常
  5、修改群在线人员和群成员列表头像的下载显示
 2013.8.29
  1. 修改UserInformationActivity.flushUserInfo, 处理部分用户头像为默认头像的问题；取消像册圆角化。
  2. 修改AlbumGridViewAdapter，处理像册显示不正确的问题。
  3. 修改GroupMsgRoom, 解决有时有些头像一直不下载的问题。
  4. 修改FileTransfer，增加取消任务的功能。
  5、去掉了上传任务的mbDone标记和listener,都统一调用父类的方法
  6、统一了对任务异常、和重试策略的处理

 2013.8.30
  1.取消聊天室中上传图片"发送中"，进度修改。
  2.转发微博，短信，邮件文案修改。
 2013.9.2
  1.修改GroupMember，优化加载逻辑
  2.微博转发修改(策略和文案)。
  3.单聊中 发送图片崩溃问题(ui问题)。

 2013.09.03
  1、修改查看聊天室和头像查看大图
  2、修改相册大图滑动在部分机型上晃动的问题，改为viewPager实现
  3、修改单聊头像不显示的bug
  4、
  
 2013.09.04
  1、整理TX访问数据库。修改idNotInvilid为isIdValid。
  2、修改服务自动重传失败的图片消息时，解决上传未合成图片的错误。
  
 2013.9.5
  1. 修改FileTransfer, 修改错误处理方式，并修改doTask异常处理。
  2. 修改FileTransfer，修改TSM逻辑，强化错误检查。
  3. 修改EditSendImg.java, TouchZoomView.java, 在VIEW初始化时将图片置为满屏
  
 20130.9.06
  1、整理了访问数据库获取TX的功能，优化了getTXMe()等方法
  2013.09.09
  1.修改新浪微博登录设置密码(加了SetUpdateOtherPassWordActivity和对应的布局文件)
  2、修改了TX中updateTXMe()方法的调用的所有地方，后续还需要整理便于管理
  3、修改了上传图片显示上传进度的progressBar的样式
  2013.09.10
  1.添加清除缓存清除image目录的文件。
  2、修改了隐式登陆好友无法正常加载的bug，是因为在第一个indexActivity查询数据库时，返回的cursor为空，此时数据库没有初始化，数据库的初始化在登陆或注册成功以后
  3、修改了部分相册地址路径为空时，生成文件名异常的bug
  4、修改了切换账号时设置页面头像没有更换的问题。因为切换账号时TX中user_id没有置为-1；
  
  2013.09.11
  1、查看相册大图时可以保存该图片
  2013.09.12
  1、聊天室中发送的名片中，头像可以正常显示
  2、修改了群组动态不显示头像的问题
  3、修改单聊时，有时对方头像不显示或性别不正确的问题
  2013.09.13
  1、增加了聊天室中删除正在上传或者下载的消息时，同时删除上传下载任务的功能
  2、在UserAlbumActivity中增加广播，可以创建弹窗显示系统消息。--春会
  3、修改ClientManager，聊天室中只创建一个ClientManager对象控制音频的录制和播放，在TxData中初始化。----顾问修改
  
  2013.9.16
  1. 修正9.13改出的BUG,支持iOSVBR编码
  2.修改聊天室中，查看个人资料，和群成员时，消息栏还会收到消息提醒的bug。
  3、修改合成相册图片的策略
  4、解决选择好友列表，头像晃动问题。
  5、查看其他人资料（有相册）时，有时先显示暂时没有照片，再显示照片
  6、解决 tab->消息->消息列表->进入聊天室，退出聊天室，聊天头像晃动 
  2013.09.17
  1.登录记住密码，按菜单键退出后，密码没记住---汇川修改。
  
  2013.9.18
  1.解决消息会话列表头像不正确的问题，同时删除了无用代码
  2.解决不能自动连接播放的BUG(新版BUG)
  3、整理了TX是否在黑名单里的字段in_black_list的赋值，但还是没有解决神聊用户莫名其妙进入黑名单的问题。shc
  4、修改黑名单列表头像不显示是问题。（应该是修改AsynchImageLoader时改出来的bug）
  
  2013.09.22
  1、修改了TX中tx_ids的存取操作，不再通过访问数据库获取。
  2、修改神聊好友会进入黑名单的问题，是在TX的setBundle()方法里，获取是否为黑名单用户时，没有设定默认值-1。--顾问指导修改
  3、修改登录activity中，当软键盘弹出后界面向上移动，选择账号的popupWindow显示的位置会向上偏移的问题。
  4、修改了上传相册后，相册缩略图闪一下的问题。

  2013.9.23
  1. 修改语音消息的播放状态的实现方式, 以解决语音消息播放状态不对的BUG。
  2、删除BLog类，所有log开关都由Utils.debug控制
  3. 修改放音接口名，隐藏ClientManager.setRunning(boolean)方法
  4. 修改最后聊天室最后10条的处理策略，解决最后10条中语音总是未读的BUG. 
  5、更改UserInformationActivity当用户头像版本有更新时，修改小头像的同时把大头像删除，以免查看大图时还是老头像。
  6、修改聊天室修改头像后，查看聊天室详情和编辑聊天室时头像不一致的问题。是因为两个activity取头像时命名不一致导致。
  7、修改消息会话列表只有一个人时，头像闪动的问题。
  
  2013.09.24
  1、修改个人相册第一个图片缩略图有时不显示的bug  顾问指导
  2、没有头像好有点击崩溃的bug (修改更新头像大图时，改出来的bug)
  3、修改群聊没头像的bug(头像下载成功没有缓存，改出来的bug)
  4、修改单聊和消息会话页面，陌生人头像显示为默认头像的bug
  5、修改加入聊天室头像不显示的问题。
  
  2013.09.25
  1.修改神聊客服相册，头像不能点击，点击查看不了大图，保存不到本地问题。
  2、修改MessageActivity多次切换头像为空白的问题，是因为个人头像ImageView的tag为-partner_id。回调中判断时没有加-号，改出来的bug
  3、增加重发图片的方法，与第一次上传图片不同。上传成功不是保存消息到数据库而是修改数据库中的消息。
  4、修改群资料页头像不更新的问题。没有完美解决，现在是强制去从服务器下载新头像，后续可以考虑把群头像名字与url关联。
  5、TX中getBundle()方法调用前先调用buildBundle()把所有的字段设置到bundle中。把所有set方法中添加到bundle的方法去掉。
  6、在TX的setBundle中增加set的字段，保证和buildBundle方法中所有字段一致。
  
  2013.09.26
  1、修改群聊天室中点击“***进入聊天室”时，进入个人资料页，头像默认为女，并且加载不到头像的问题。
  2、修改TX的saveTXtoDB更新_id方法。改出来的bug.(修改的问题是：部分用户查看用户信息后，举报该用户，显示的个人信息不正确。)
  3、解决这个问题：修改群头像后，没有点击保存，按返回键，部分页面群头像更新。多次切换页面后，群头像能恢复到以前。（用户没有点击保存，不应该改变群头像）
  4、修改聊天室中选择要发送的图片，没有发送，点击返回，再次发送文字消息，显示一个空图片消息汽包的bug  春会
  5、修改获取离线消息获取错误或者获取不到的问题。每次进聊天室取离线消息时，置gmid为空字符串。代表取聊天室最新10条消息。
  
  2013.09.27
  1、修改了，因为消息会话列表有个人重复数据，一直收不到最新消息的问题。
  2、修改消息会话页面头像加载成功后，在回调中缓存。
  3、修改了A在群中发言后，B消息会话列表第一个是群，A又给B单聊发消息，B消息会话列表中的群会话消失的bug。
  	   因为：com.tuixin11sms.tx.message.MsgStat.saveMsgStattoDB(MsgStat, TxData)更新数据库有问题，查询个人会话时用partner_id有问题，如果和群消息的发言人partner_id一样，会查到群的那条记录，把群会话记录给覆盖了。
	        需要修改TxDB.MsgStat表，增加会话类型字段。
	      
	      
  2013.09.29
  1、修改了消息列表群头像会变成个人头像的问题，原因是：如果群会话条目复用了个人会话条目，群头像从缓存中取命中后，没有设置。
  
  2013.09.30
  1、删除ContactAPISdk3及其引用。此类只在SDK<5时用到，但是工程要求最低SDK版本为5。故无用了。
  2、整理好友tab页面加载好友的功能，避免好友丢失，待测试。
  3、整理TX关于tx的增删改查，统一入口和管理，未完成。
  
  2013.10.09
  1、增加了登陆成功后给FileTransfer的mTSLogonPara设置uid和token参数。
  2、整理TuixinService，合并onCreate和onStart方法中重复的内容。
  3、增加服务是否开启的标记，当收到【登陆成功】的应答时，如果服务没有开启，则等待200毫秒再去判断。直到服务开启才去发送登陆成功的广播。
  
  2013.10.10
  1、整理删除聊天室消息布局文件中无用的控件。
  2、解决用户手机没有安装地图应用时点击地理位置崩溃的问题。
  3、解决用户手机未知原因崩溃导致的丢好友问题。
  
  2013.10.12
  1、整合单聊、群聊到BaseMsgRoom。删除部分重复或无用代码。
  
  2013.10.14
  1、整理activity中txdata调用基类BaseActivity中的txdata。
  2、删除Utils中screeScoW的相关引用。无用变量。
  
  2013.10.18
  1、屏蔽其他类直接访问TX数据库，其他类均只操作TX持有的缓存，由TX缓存来操作数据库
  
  2013.10.19
  1、修改了msgstat表结构，增加了sessionId和msgstat_type字段，对表中错误数据进行了筛选删除
  2、修改了消息会话MsgStat的缓存数据存取方法。避免外部类直接操作数据库。
  
  
  2013.10.22
  1、修改了第二次登陆，好友获取不到的问题。因为MessageActivity也创建了一个数据库，存放数据库有问题。

  2014.01.23 
  1、把DataContainer中的黑名单list放到TX中管理。
  2、去掉了TXMessage、MsgStat、TX中无效的字段及其引用。（在TxDBContentProvider的upgradeDatabaseToVersion16方法中有详细记录）
  3、给TxGroup增加缓存处理
  
