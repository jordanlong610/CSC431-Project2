import java.util.zip.Checksum;
public class BasicChecksum implements Checksum
{
	char[] checksum = new char[8];

	@Override
	public void update(int b)
	{

	}

	@Override
	public void update(byte[] b, int off, int len)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public long getValue()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reset()
	{
		// TODO Auto-generated method stub

	}

}
