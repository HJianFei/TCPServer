package validation;

public class BaseValidationHelper implements AbsValidationHelper {
	@Override
	public boolean execute(byte[] msg) {
		return true;
	}
}
