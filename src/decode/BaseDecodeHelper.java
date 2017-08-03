package decode;

import bean.TargetInfo;
import config.TcpConnConfig;

public class BaseDecodeHelper implements AbsDecodeHelper {
	@Override
	public byte[][] execute(byte[] data, TargetInfo targetInfo, TcpConnConfig tcpConnConfig) {
		return new byte[][] { data };
	}
}
