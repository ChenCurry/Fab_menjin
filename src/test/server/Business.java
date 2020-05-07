package test.server;

import java.sql.SQLException;

import javax.swing.tree.DefaultMutableTreeNode;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;

import test.dao.UserDao;
import test.dao.impl.UserDaoImpl;
import test.domain.AlarmInfo;
import test.util.HCNetSDK;

public class Business {

	HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
	//PlayCtrl playControl = PlayCtrl.INSTANCE;
	NativeLong g_lVoiceHandle;// 全局的语音对讲句柄
	String username = System.getProperties().getProperty("user.name");

	HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;// 设备信息
	HCNetSDK.NET_DVR_IPPARACFG m_strIpparaCfg;// IP参数
	HCNetSDK.NET_DVR_CLIENTINFO m_strClientInfo;// 用户参数
	Boolean bRealPlay;// 是否在预览.
	String m_sDeviceIP;// 已登录设备的IP地址
	NativeLong lUserID;// 用户句柄
	NativeLong lPreviewHandle;// 预览句柄
	NativeLongByReference m_lPort;// 回调预览时播放库端口指针
	NativeLong lAlarmHandle;// 报警布防句柄
	NativeLong lListenHandle;// 报警监听句柄
	Integer count = 0;
	FMSGCallBack fMSFCallBack;// 报警回调函数实现
	FMSGCallBack_V31 fMSFCallBack_V31;// 报警回调函数实现
//  FMSGCallBack fMSFCallBack;//报警回调函数实现
//  FRealDataCallBack fRealDataCallBack;//预览回调函数实现
//  JFramePTZControl framePTZControl;//云台控制窗口
	int m_iTreeNodeNum;// 通道树节点数目
	DefaultMutableTreeNode m_DeviceRoot;// 通道树根节点
	String ipAd;
	String deNo;
	String localPort;
	private UserDao userDao=new UserDaoImpl();

	// 内部类: FMSGCallBack_V31 报警信息回调函数
	public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {
		// 报警信息回调函数
		public boolean invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen,
				Pointer pUser) {
			AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
			return true;
		}
	}

	// 内部类: FMSGCallBack 报警信息回调函数
	public class FMSGCallBack implements HCNetSDK.FMSGCallBack {
		// 报警信息回调函数
		public void invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen,
				Pointer pUser) {
			AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
		}
	}

	// 报警数据处理
	public void AlarmDataHandle(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo,
			int dwBufLen, Pointer pUser) {
		System.out.println(deNo + "报警信息回调函数执行" + ++count);
		//String sAlarmType = new String();
		// DefaultTableModel alarmTableModel = ((DefaultTableModel)
		// jTableAlarm.getModel());//获取表格模型
		//String[] newRow = new String[3];
		// 报警时间
		//Date today = new Date();
		//DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String[] sIP = new String[2];
		// lCommand是传的报警类型
		//sAlarmType = new String("lCommand=") + lCommand.intValue();
		// System.out.println("报警类型lCommand值为-->"+lCommand+",pAlarmer为-->"+pAlarmer+",pAlarmInfo为-->"+pAlarmInfo+",dwBufLen为-->"+dwBufLen+",pUser为-->"+pUser);
		switch (lCommand.intValue()) {
//		case HCNetSDK.COMM_ALARM_V30:
//			break;
//		case HCNetSDK.COMM_ALARM_RULE:
//			break;
//		case HCNetSDK.COMM_UPLOAD_PLATE_RESULT:
//			break;
//		case HCNetSDK.COMM_ITS_PLATE_RESULT:
//			break;
//		case HCNetSDK.COMM_ALARM_PDC:
//			break;
//		case HCNetSDK.COMM_ITS_PARK_VEHICLE:
//			break;
//		case HCNetSDK.COMM_ALARM_TFS:
//			break;
//		case HCNetSDK.COMM_ALARM_AID_V41:
//			break;
//		case HCNetSDK.COMM_ALARM_TPS_V41:
//			break;
//		case HCNetSDK.COMM_UPLOAD_FACESNAP_RESULT:
//			break;
//		case HCNetSDK.COMM_SNAP_MATCH_ALARM://人脸黑名单比对报警
//			break;
		case HCNetSDK.COMM_ALARM_ACS: // 门禁主机报警信息
			HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo = new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
			strACSInfo.write();
			Pointer pACSInfo = strACSInfo.getPointer();
			pACSInfo.write(0, pAlarmInfo.getByteArray(0, strACSInfo.size()), 0, strACSInfo.size());
			strACSInfo.read();

			/*
			 * sAlarmType = sAlarmType + "：门禁主机报警信息，卡号：" + new
			 * String(strACSInfo.struAcsEventInfo.byCardNo).trim() + "，卡类型：" +
			 * strACSInfo.struAcsEventInfo.byCardType + "，报警主类型：" + strACSInfo.dwMajor +
			 * "，报警次类型：" + strACSInfo.dwMinor;
			 */

			//newRow[0] = dateFormat.format(today);
			// 报警类型
			//newRow[1] = sAlarmType;
			// 报警设备IP地址
			sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
			//newRow[2] = sIP[0];
			// alarmTableModel.insertRow(0, newRow);
			 //System.out.println(deNo+"门禁主机报警信息-->newRow[0]:"+newRow[0]+"newRow[1]:"+newRow[1]+"newRow[2]:"+newRow[2]);
			String cardNo = new String(strACSInfo.struAcsEventInfo.byCardNo).trim();
			if (null != cardNo && !"".equals(cardNo)) {
				String cardTyp = strACSInfo.struAcsEventInfo.byCardType + "";
				String dwMajor = strACSInfo.dwMajor + "";
				String dwMinor = strACSInfo.dwMinor + "";
				String struTime = strACSInfo.struTime.toStringTime();// NET_DVR_TIME struTime;
				String dwCardReaderNo = strACSInfo.struAcsEventInfo.dwCardReaderNo + "";
				String dwIOTChannelNo = strACSInfo.dwIOTChannelNo + "";// IOT通道号
				String dwDoorNo = strACSInfo.struAcsEventInfo.dwDoorNo + "";

				/*
				 * String sNetUser = new String(strACSInfo.sNetUser).trim();// byte[] sNetUser
				 * String dwSize = strACSInfo.dwSize + ""; String struRemoteHostAddr =
				 * strACSInfo.struRemoteHostAddr + "";// NET_DVR_IPADDR struRemoteHostAddr
				 * String dwPicDataLen = strACSInfo.dwPicDataLen + ""; String
				 * wInductiveEventType = strACSInfo.wInductiveEventType + "";//
				 * 归纳事件类型，0-无效，客户端判断该值为非0值后，报警类型通过归纳事件类型区分，否则通过原有报警主次类型（dwMajor、dwMinor）区分
				 * String byPicTransType = strACSInfo.byPicTransType + "";// 图片数据传输方式:
				 * 0-二进制；1-url String byRes1 = strACSInfo.byRes1 + "";// 保留字节
				 */				
				// byte[] byRes
				// struAcsEventInfo 报警信息详细参数
				// String struAcsEventInfo =
				// strACSInfo.struAcsEventInfo.toString();//NET_DVR_ACS_EVENT_INFO
				// struAcsEventInfo
				
				
				//String byCardNo = new String(strACSInfo.struAcsEventInfo.byCardNo).trim();// byte[] byCardNo
				//String byCardType = strACSInfo.struAcsEventInfo.byCardType + "";
				/*
				 * String dwSize_ = strACSInfo.struAcsEventInfo.dwSize + "";// 结构体大小
				 * String byWhiteListNo = strACSInfo.struAcsEventInfo.byWhiteListNo + ""; String
				 * byReportChannel = strACSInfo.struAcsEventInfo.byReportChannel + ""; String
				 * byCardReaderKind = strACSInfo.struAcsEventInfo.byCardReaderKind + "";
				 *  String dwVerifyNo =
				 * strACSInfo.struAcsEventInfo.dwVerifyNo + ""; String dwAlarmInNo =
				 * strACSInfo.struAcsEventInfo.dwAlarmInNo + ""; String dwAlarmOutNo =
				 * strACSInfo.struAcsEventInfo.dwAlarmOutNo + ""; String dwCaseSensorNo =
				 * strACSInfo.struAcsEventInfo.dwCaseSensorNo + ""; String dwRs485No =
				 * strACSInfo.struAcsEventInfo.dwRs485No + ""; String dwMultiCardGroupNo =
				 * strACSInfo.struAcsEventInfo.dwMultiCardGroupNo + ""; String wAccessChannel =
				 * strACSInfo.struAcsEventInfo.wAccessChannel + ""; String byDeviceNo =
				 * strACSInfo.struAcsEventInfo.byDeviceNo + ""; String byDistractControlNo =
				 * strACSInfo.struAcsEventInfo.byDistractControlNo + ""; String dwEmployeeNo =
				 * strACSInfo.struAcsEventInfo.dwEmployeeNo + ""; String wLocalControllerID =
				 * strACSInfo.struAcsEventInfo.wLocalControllerID + ""; String byInternetAccess
				 * = strACSInfo.struAcsEventInfo.byInternetAccess + ""; String byType =
				 * strACSInfo.struAcsEventInfo.byType + ""; String byRes = new
				 * String(strACSInfo.struAcsEventInfo.byRes).trim();// byte[] byRes
				 */
				AlarmInfo alarmInfo = new AlarmInfo();
				alarmInfo.setAdrass(sIP[0]);
				alarmInfo.setCarNo(cardNo);
				alarmInfo.setDwCardReaderNo(dwCardReaderNo);
				alarmInfo.setStruTime(struTime);
				alarmInfo.setCardType(cardTyp);
				alarmInfo.setDwMajor(dwMajor);
				alarmInfo.setDwMinor(dwMinor);
				alarmInfo.setDwDoorNo(dwDoorNo);
				alarmInfo.setDwIOTChannelNo(dwIOTChannelNo);
				
				
				/*
				 * alarmInfo.setsNetUser(sNetUser);
				 * alarmInfo.setwInductiveEventType(wInductiveEventType);
				 * alarmInfo.setByWhiteListNo(byWhiteListNo);
				 * alarmInfo.setByReportChannel(byReportChannel);
				 * alarmInfo.setByCardReaderKind(byCardReaderKind);
				 * alarmInfo.setDwVerifyNo(dwVerifyNo); alarmInfo.setDwAlarmInNo(dwAlarmInNo);
				 * alarmInfo.setDwAlarmOutNo(dwAlarmOutNo);
				 * alarmInfo.setDwCaseSensorNo(dwCaseSensorNo);
				 * alarmInfo.setDwRs485No(dwRs485No);
				 * alarmInfo.setDwMultiCardGroupNo(dwMultiCardGroupNo);
				 * alarmInfo.setwAccessChannel(wAccessChannel);
				 * alarmInfo.setByDeviceNo(byDeviceNo);
				 * alarmInfo.setByDistractControlNo(byDistractControlNo);
				 * alarmInfo.setDwEmployeeNo(dwEmployeeNo);
				 * alarmInfo.setwLocalControllerID(wLocalControllerID);
				 * alarmInfo.setByInternetAccess(byInternetAccess); alarmInfo.setByType(byType);
				 */
				
				/*
				 * TestDao testDao = new TestDao(deNo); testDao.insertInfo(alarmInfo);
				 */
				if(null!=alarmInfo&&null!=alarmInfo.getAdrass()) {
					try {
						userDao.insertInfo(alarmInfo);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			break;
		case HCNetSDK.COMM_ID_INFO_ALARM: // 身份证信息
			break;
		default:
			// newRow[0] = dateFormat.format(today);
			// 报警类型
			// newRow[1] = sAlarmType;
			// 报警设备IP地址
			// sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
			// newRow[2] = sIP[0];
			// alarmTableModel.insertRow(0, newRow);
			// System.out.println(deNo+"default-->newRow[0]:"+newRow[0]+"newRow[1]:"+newRow[1]+"newRow[2]:"+newRow[2]);
			break;
		}
	}

	// 布防
	public void alarm() throws InterruptedException {
		// 尚未监听,开始监听
		if (lListenHandle.intValue() == -1) {
			Pointer pUser = null;
			if (fMSFCallBack == null) {
				fMSFCallBack = new FMSGCallBack();
			}
			
			lListenHandle = hCNetSDK.NET_DVR_StartListen_V30(null, Short.parseShort(localPort), fMSFCallBack, pUser);
			//System.out.println(deNo + "启动监听lListenHandle："+lListenHandle);
			if (lListenHandle.intValue() < 0) {
				System.out.println(deNo + "启动监听失败");
			} else {
				System.out.println(deNo + "启动监听成功");
			}
			// 设置布防
			if (lAlarmHandle.intValue() < 0) {
				if (fMSFCallBack_V31 == null) {
					fMSFCallBack_V31 = new FMSGCallBack_V31();
					pUser = null;
					// 设置报警回调函数
					boolean flag = hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser);
					// System.out.println("设置报警回调函数true表示设置成功:"+flag);
					if (!flag) {
						System.out.println(deNo + "设置回调函数失败");
					}else {
						System.out.println(deNo + "设置回调函数成功");
					}
				}
				HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
				m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
				m_strAlarmInfo.byLevel = 1;
				m_strAlarmInfo.byAlarmInfoType = 1;
				m_strAlarmInfo.byDeployType = 1;
				m_strAlarmInfo.write();
				lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
				//System.out.println(deNo + "设置布防成败标识lAlarmHandle:" + lAlarmHandle);
				if (lAlarmHandle.intValue() == -1) {
					// JOptionPane.showMessageDialog(null, "布防失败");
					System.out.println(deNo + "布防失败");
				} else {
					// JOptionPane.showMessageDialog(null, "布防成功");
					System.out.println(deNo + "布防成功");
				}
				while (true) {
					Thread.sleep(60000 * 60 * 24);
				}
			}
		}
		// 不需要撤防
//		if (lAlarmHandle.intValue() != -1) {
//			if (!hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle)) {
//				System.out.println(deNo + "撤防失败");
//				lAlarmHandle = new NativeLong(-1);
//			} else {
//				lAlarmHandle = new NativeLong(-1);
//				System.out.println(deNo + "撤防执行");
//			}
//		}
	}

	// 注册设备
	public void register() throws InterruptedException {
		// 初始化
		boolean initSuc = hCNetSDK.NET_DVR_Init();
		if (initSuc != true) {
			// JOptionPane.showMessageDialog(null, deNo+"初始化失败");
			System.out.println(deNo + "初始化失败");
		}

		// 设置连接时间 和 重连
		hCNetSDK.NET_DVR_SetConnectTime(2000, 1);
		hCNetSDK.NET_DVR_SetReconnect(10000, true);

		if (lUserID.longValue() > -1) {
			// 先注销
			hCNetSDK.NET_DVR_Logout_V30(lUserID);
			lUserID = new NativeLong(-1);
			m_iTreeNodeNum = 0;
			m_DeviceRoot.removeAllChildren();
		}

		/*
		 * 注册 192.168.1.154 192.168.1.155 192.168.1.156 8000 admin admin123!
		 */
		m_sDeviceIP = ipAd;// 设备ip地址
		m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
		int iPort = 8000;
		// 注册
		lUserID = hCNetSDK.NET_DVR_Login_V30(m_sDeviceIP, (short) iPort, "admin", "admin123!", m_strDeviceInfo);
		long userID = lUserID.longValue();
		if (userID == -1) {
			m_sDeviceIP = "";// 登录未成功,IP置为空
			// JOptionPane.showMessageDialog(null, deNo+"注册失败");
			System.out.println(deNo + "注册失败");
		} else {
			System.out.println(deNo + "注册成功");
			// JOptionPane.showMessageDialog(null, "注册成功");
			// CreateDeviceTree();
			// 注册成功布防
			alarm();
		}
//		// 如果已经注册,注销
//		if (lUserID.longValue() > -1) {
//			hCNetSDK.NET_DVR_Logout_V30(lUserID);
//			System.out.println(deNo + "注销执行");
//		}
//		// cleanup SDK
//		hCNetSDK.NET_DVR_Cleanup();
	}

	// 构造函数 初始化成员变量
	public Business(String ipAd,String localPort) {
		this.ipAd = ipAd;
		this.localPort = localPort;
		int len = ipAd.length();
		deNo = ipAd.substring(len - 3, len);

		lUserID = new NativeLong(-1);
		lPreviewHandle = new NativeLong(-1);
		lAlarmHandle = new NativeLong(-1);
		lListenHandle = new NativeLong(-1);
		g_lVoiceHandle = new NativeLong(-1);
		m_lPort = new NativeLongByReference(new NativeLong(-1));
		fMSFCallBack = null;
		m_iTreeNodeNum = 0;
	}

}
