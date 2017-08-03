package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import bean.TargetInfo;
import bean.TcpMsg;
import client.TcpClient;
import config.TcpServerConfig;
import listener.TcpClientListener;
import listener.TcpServerListener;
import manager.TcpServerManager;
import state.ServerState;

public class TcpServer implements TcpClientListener {

	protected int port;
	protected ServerState mServerState;
	protected ServerSocket mServerSocket;
	protected Map<TargetInfo, TcpClient> mTcpClients;
	protected ListenThread mListenThread;
	protected TcpServerConfig mTcpServerConfig;
	protected List<TcpServerListener> mTcpServerListeners;

	private TcpServer() {
		super();
	}

	public static TcpServer getTcpServer(int port) {
		TcpServer xTcpServer = TcpServerManager.getTcpServer(port);
		if (xTcpServer == null) {
			xTcpServer = new TcpServer();
			xTcpServer.init(port);
			TcpServerManager.putTcpServer(xTcpServer);
		}
		return xTcpServer;
	}

	private void init(int port) {
		this.port = port;
		setServerState(ServerState.Closed);
		mTcpClients = new LinkedHashMap<>();
		mTcpServerListeners = new ArrayList<>();
		if (mTcpServerConfig == null) {
			mTcpServerConfig = new TcpServerConfig.Builder().create();
		}
	}

	public void startServer() {
		if (!getListenThread().isAlive()) {
			System.out.println("tcp server启动中... ");
			getListenThread().start();
		}
	}

	public void stopServer() {
		stopServer("手动关闭tcpServer", null);
	}

	protected void stopServer(String msg, Exception e) {
		getListenThread().interrupt();// 关闭listen
		setServerState(ServerState.Closed);
		if (closeSocket()) {
			for (TcpClient client : mTcpClients.values()) {
				if (client != null) {
					client.disconnect();
				}
			}
			notifyTcpServerClosed(msg, e);
		}
		System.out.println("tcp server closed");
	}

	private boolean closeSocket() {
		if (mServerSocket != null && !mServerSocket.isClosed()) {
			try {
				mServerSocket.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean sendMsgToAll(TcpMsg msg) {
		boolean re = true;
		for (TcpClient client : mTcpClients.values()) {
			if (client.sendMsg(msg) == null) {
				re = false;
			}
		}
		return re;
	}

	public boolean sendMsgToAll(String msg) {
		boolean re = true;
		for (TcpClient client : mTcpClients.values()) {
			if (client.sendMsg(msg) == null) {
				re = false;
			}
		}
		return re;
	}

	public boolean sendMsgToAll(byte[] msg) {
		boolean re = true;
		for (TcpClient client : mTcpClients.values()) {
			if (client.sendMsg(msg) == null) {
				re = false;
			}
		}
		return re;
	}

	public boolean sendMsg(TcpMsg msg, TcpClient client) {
		return client.sendMsg(msg) != null;
	}

	public boolean sendMsg(String msg, TcpClient client) {
		return client.sendMsg(msg) != null;
	}

	public boolean sendMsg(byte[] msg, TcpClient client) {
		return client.sendMsg(msg) != null;
	}

	public boolean sendMsg(TcpMsg msg, String ip) {
		TcpClient client = mTcpClients.get(ip);
		if (client != null) {
			return client.sendMsg(msg) != null;
		}
		return false;
	}

	public boolean sendMsg(String msg, String ip) {
		TcpClient client = mTcpClients.get(ip);
		if (client != null) {
			return client.sendMsg(msg) != null;
		}
		return false;
	}

	public boolean sendMsg(byte[] msg, String ip) {
		TcpClient client = mTcpClients.get(ip);
		if (client != null) {
			return client.sendMsg(msg) != null;
		}
		return false;
	}

	class ListenThread extends Thread {
		@Override
		public void run() {
			Socket socket;
			while (!Thread.interrupted()) {
				try {
					System.out.println("tcp server 监听中...");
					socket=getServerSocket().accept();
					TargetInfo targetInfo = new TargetInfo(socket.getInetAddress().getHostAddress(), socket.getPort());
					TcpClient xTcpClient = TcpClient.getTcpClient(socket, targetInfo,
							mTcpServerConfig.getTcpConnConfig());// 创建一个client，接受和发送消息
					notifyTcpServerAccept(xTcpClient);
					xTcpClient.addTcpClientListener(TcpServer.this);
					mTcpClients.put(targetInfo, xTcpClient);
				} catch (IOException e) {
					System.out.println("tcp server listening error:" + e);
					// e.printStackTrace();
					stopServer("监听失败", e);
					// return;
				}
			}
		}
	}

	protected ListenThread getListenThread() {
		if (mListenThread == null || !mListenThread.isAlive()) {
			mListenThread = new ListenThread();
		}
		return mListenThread;
	}

	protected ServerSocket getServerSocket() {
		if (mServerSocket == null || mServerSocket.isClosed()) {
			try {
				mServerSocket = new ServerSocket(port);
				setServerState(ServerState.Created);
				notifyTcpServerCreate();
				setServerState(ServerState.Listening);
				notifyTcpServerLinten();
			} catch (IOException e) {
				// e.printStackTrace();
				stopServer("创建失败", e);
			}
		}
		return mServerSocket;
	}

	public void addTcpServerListener(TcpServerListener listener) {
		if (mTcpServerListeners.contains(listener)) {
			return;
		}
		this.mTcpServerListeners.add(listener);
	}

	public void removeTcpServerListener(TcpServerListener listener) {
		this.mTcpServerListeners.remove(listener);
	}

	private void notifyTcpServerCreate() {
		for (TcpServerListener wr : mTcpServerListeners) {
			final TcpServerListener l = wr;
			if (l != null) {
				l.onCreated(TcpServer.this);
			}
		}
	}

	private void notifyTcpServerLinten() {
		for (TcpServerListener wr : mTcpServerListeners) {
			final TcpServerListener l = wr;
			if (l != null) {
				l.onListened(TcpServer.this);

			}
		}
	}

	private void notifyTcpServerAccept(final TcpClient client) {
		for (TcpServerListener wr : mTcpServerListeners) {
			final TcpServerListener l = wr;
			if (l != null) {

				l.onAccept(TcpServer.this, client);

			}
		}
	}

	private void notifyTcpServerReceive(final TcpClient client, final TcpMsg tcpMsg) {
		for (TcpServerListener wr : mTcpServerListeners) {
			final TcpServerListener l = wr;
			if (l != null) {
				l.onReceive(TcpServer.this, client, tcpMsg);
			}
		}
	}

	private void notifyTcpServerSended(final TcpClient client, final TcpMsg tcpMsg) {
		for (TcpServerListener wr : mTcpServerListeners) {
			final TcpServerListener l = wr;
			if (l != null) {
				l.onSended(TcpServer.this, client, tcpMsg);
			}
		}
	}

	private void notifyTcpServerValidationFail(final TcpClient client, final TcpMsg tcpMsg) {
		for (TcpServerListener wr : mTcpServerListeners) {
			final TcpServerListener l = wr;
			if (l != null) {
				l.onValidationFail(TcpServer.this, client, tcpMsg);
			}
		}
	}

	private void notifyTcpClientClosed(final TcpClient client, final String msg, final Exception e) {
		for (TcpServerListener wr : mTcpServerListeners) {
			final TcpServerListener l = wr;
			if (l != null) {
				l.onClientClosed(TcpServer.this, client, msg, e);
			}
		}
	}

	private void notifyTcpServerClosed(final String msg, final Exception e) {
		for (TcpServerListener wr : mTcpServerListeners) {
			final TcpServerListener l = wr;
			if (l != null) {
				l.onServerClosed(TcpServer.this, msg, e);
			}
		}
	}

	private void setServerState(ServerState state) {
		this.mServerState = state;
	}

	public int getPort() {
		return port;
	}

	public boolean isClosed() {
		return mServerState == ServerState.Closed;
	}

	public boolean isListening() {
		return mServerState == ServerState.Listening;
	}

	public void config(TcpServerConfig tcpServerConfig) {
		mTcpServerConfig = tcpServerConfig;
	}

	@Override
	public void onConnected(TcpClient client) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSended(TcpClient client, TcpMsg tcpMsg) {
		// TODO Auto-generated method stub
		notifyTcpServerSended(client, tcpMsg);

	}

	@Override
	public void onDisconnected(TcpClient client, String msg, Exception e) {
		// TODO Auto-generated method stub
		notifyTcpClientClosed(client, msg, e);

	}

	@Override
	public void onReceive(TcpClient client, TcpMsg tcpMsg) {
		// TODO Auto-generated method stub
		notifyTcpServerReceive(client, tcpMsg);

	}

	@Override
	public void onValidationFail(TcpClient client, TcpMsg tcpMsg) {
		// TODO Auto-generated method stub
		notifyTcpServerValidationFail(client, tcpMsg);

	}

}
