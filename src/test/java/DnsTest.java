import com.github.jdiverttunnel.weynidrop.common.constants.IpConstants;
import com.github.jdiverttunnel.weynidrop.common.utils.IpUtils;
import org.junit.Test;
import sun.net.util.IPAddressUtil;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
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
public class DnsTest {

    @Test
    public void testDns(){
        String domainName = "github.com";

        Set<String> ips = IpUtils.getAllIP(domainName);
        System.out.println(domainName + ": "+ ips.toString());
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {

    }

}