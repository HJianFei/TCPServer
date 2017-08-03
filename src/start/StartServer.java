package start;

import bean.TcpMsg;
import client.TcpClient;
import config.TcpConnConfig;
import config.TcpServerConfig;
import listener.TcpServerListener;
import server.TcpServer;

public class StartServer {

	private static TcpServer mTcpServer;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (mTcpServer != null && mTcpServer.isListening()) {
			mTcpServer.stopServer();
		} else {
			if (mTcpServer == null) {
				mTcpServer = TcpServer.getTcpServer(8989);
				mTcpServer.config(
						new TcpServerConfig.Builder().setTcpConnConfig(new TcpConnConfig.Builder().create()).create());
				mTcpServer.addTcpServerListener(new TcpServerListener() {

					@Override
					public void onValidationFail(TcpServer server, TcpClient client, TcpMsg tcpMsg) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onServerClosed(TcpServer server, String msg, Exception e) {
						// TODO Auto-generated method stub
						System.out.println("服务器关闭 " + server + msg + e);

					}

					@Override
					public void onSended(TcpServer server, TcpClient tcpClient, TcpMsg tcpMsg) {
						// TODO Auto-generated method stub
						System.out.println("发送消息给 " + tcpClient.getTargetInfo().getIp() + " 成功 msg= "
								+ tcpMsg.getSourceDataString());

					}

					@Override
					public void onReceive(TcpServer server, TcpClient tcpClient, TcpMsg tcpMsg) {
						// TODO Auto-generated method stub
						System.out.println("收到客户端消息: " + tcpMsg.toString());
						tcpClient.sendMsg("信息已收到："+tcpMsg.getSourceDataString());

					}

					@Override
					public void onListened(TcpServer server) {
						// TODO Auto-generated method stub
						System.out.println("服务监听中... ");

					}

					@Override
					public void onCreated(TcpServer server) {
						// TODO Auto-generated method stub
						System.out.println("服务器启动成功");

					}

					@Override
					public void onClientClosed(TcpServer server, TcpClient tcpClient, String msg, Exception e) {
						// TODO Auto-generated method stub
						System.out.println("客户端连接断开 " + tcpClient.getTargetInfo().getIp() + msg + e);

					}

					@Override
					public void onAccept(TcpServer server, TcpClient tcpClient) {
						// TODO Auto-generated method stub
						System.out.println("收到客户端连接请求 " + tcpClient.getTargetInfo().getIp());

					}
				});

			}
			mTcpServer.startServer();
		}
	}
}
