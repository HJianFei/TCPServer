package manager;

import java.util.HashSet;
import java.util.Set;

import server.TcpServer;

public class TcpServerManager {

	private static Set<TcpServer> mTcpServers = new HashSet<>();

	public static void putTcpServer(TcpServer XTcpServer) {
		mTcpServers.add(XTcpServer);
	}

	public static TcpServer getTcpServer(int port) {
		for (TcpServer ts : mTcpServers) {
			if (ts.getPort() == port) {
				return ts;
			}
		}
		return null;
	}
}
