import com.github.jdiverttunnel.weynidrop.common.utils.IpUtils;
import com.github.ffalcinelli.jdivert.Enums;
import com.github.ffalcinelli.jdivert.Packet;
import com.github.ffalcinelli.jdivert.WinDivert;
import org.junit.Test;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author 88469401
 * @date 2019/12/30
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class IpTest {
    @Test
    public void getCityInfo() throws Exception {
//        String ip = "43.247.24.0";
//        System.out.println(IpUtils.getCityInfo(ip));
//        System.out.println(IpUtils.isChineseMainLandIp(ip));


        InetSocketAddress socketAddress = new InetSocketAddress("hawkspeed.net",0);
        System.out.println(socketAddress.isUnresolved());
//        System.out.println(socketAddress.getHostName());
//        System.out.println(socketAddress.getPort());
    }

    @Test
    public void test() throws UnknownHostException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("180.101.49.11",0);
        byte[] addrBytes = {0,0,0,0};
        InetAddress addr = InetAddress.getByAddress(addrBytes);

        System.out.println(inetSocketAddress.getAddress().getHostName());

        ByteBuffer buf = ByteBuffer.wrap(addr.getAddress());
        for (int i = 0;i<buf.limit();i++){
            System.out.println(buf.get());
        }
        System.out.println(isIp4("23.135.2.255"));
        System.out.println(isIp4("222222"));
        System.out.println(isIp4("0.0.0.0"));
        System.out.println(isIp4(inetSocketAddress.getHostString()));

        System.out.println(isIp6("11111"));
        System.out.println(isIp6("::0"));
        System.out.println(isIp6("0.0.0.0"));
    }


    public static boolean isIp4(String ip){
        Pattern pattern = Pattern.compile("^(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))$");
        Matcher matcher = pattern.matcher(ip);
        return matcher.find();
    }

    public static boolean isIp6(String ip) {
        Pattern pattern = Pattern.compile("((([0-9a-f]{1,4}:){7}([0-9a-f]{1,4}|:))|(([0-9a-f]{1,4}:){6}(:[0-9a-f]{1,4}|((25[0-5]|2[0-4]\\d" +
                "|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9a-f]{1,4}:){5}(((:[0-9a-f]{1,4}){1,2})|:((25[0-5]" +
                "|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9a-f]{1,4}:){4}(((:[0-9a-f]{1,4}){1,3})|" +
                "((:[0-9a-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-f]{1,4}:)" +
                "{3}(((:[0-9a-f]{1,4}){1,4})|((:[0-9a-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?" +
                "\\d)){3}))|:))|(([0-9a-f]{1,4}:){2}(((:[0-9a-f]{1,4}){1,5})|((:[0-9a-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." +
                "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-f]{1,4}:){1}(((:[0-9a-f]{1,4}){1,6})|((:[0-9a-f]{1,4}){0,4}:((25[0-5]" +
                "|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9a-f]{1,4}){1,7})|((:[0-9a-f]{1,4})" +
                "{0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$");
        Matcher matcher = pattern.matcher(ip);
        return matcher.find();
    }

    @Test
    public void testLocalIp(){
        String[] ips = {"192.168.0.1","10.1.1.122","10.37.235.10","255.255.255.255","127.0.0.1","0.0.0.0"};
        Arrays.stream(ips).forEach(ip->{
            try {
                InetAddress addr = InetAddress.getByName(ip);
                if (addr.isAnyLocalAddress()||addr.isLinkLocalAddress()||addr.isLoopbackAddress()||addr.isSiteLocalAddress()){
                    System.out.println("local");
                }else{
                    System.out.println("remote");
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void ipSearchMsTest(){
        Boolean isRunning = true;
        WinDivert winDivert = new WinDivert("outbound and (tcp or udp)",Enums.Layer.NETWORK,0,Enums.Flag.SNIFF);
        try {
            winDivert.open();
            while (isRunning) {
                // read a single packet
                Packet packet = winDivert.recv();
//                System.out.println(packet);
                long oldTime = System.currentTimeMillis();
                String ip = packet.getDstAddr();
                Boolean isChineseIp = IpUtils.isChineseMainLandIp(ip);
                if (isChineseIp == null){
                    continue;
                }
                if (isChineseIp){
                    System.out.println("is chinese, ip:"+ip);
                }else{
                    System.out.println("not chinese, ip:"+ip);
                }
                System.out.println("use time:"+(System.currentTimeMillis() - oldTime));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
