package manager;

import java.util.HashSet;
import java.util.Set;

import bean.TargetInfo;
import client.TcpClient;

public class TcpClientManager {
	private static Set<TcpClient> mTcpClients = new HashSet<>();

	public static void putTcpClient(TcpClient XTcpClient) {
		mTcpClients.add(XTcpClient);
	}

	public static TcpClient getTcpClient(TargetInfo targetInfo) {
		for (TcpClient tc : mTcpClients) {
			if (tc.getTargetInfo().equals(targetInfo)) {
				return tc;
			}
		}
		return null;
	}
}
