# JDivertTunnel
<br>JDivertTunnel是一个简单的windows流量重定向到socks5服务器的工具，支持win7及后续系统，基于windivert及Jdivrt开发</br>
This is a simple tunnel to realize tcp/udp stream to socks5 on windows.Support windows7 and later,base from WinDivert and Jdivert.<br>

目前是一个测试版本，你可以在dev分支查看相关源码<br>
This is just a test version,you can see it at the dev branch.<br>

注意！必须用admin模式运行此程序
Tip!This project must run on admin mode

首先，配置你的socks5服务器地址<br>
First,config your socks5server:<br>


    #socks5 config
    socks5.server = 127.0.0.1
    socks5.serverPort = 1080
    socks5.username =
    socks5.password =
    socks5.timeOut = 5000
    socks5.useRemoteDns = false

接着写入你想代理的网址或ip，运行<br>
Then,config what ip/host you want to divert and run:<br>

    //需要代理的目标IP和端口，端口为0则代理目标IP所有端口 example divert github.com
    List<InetSocketAddress> proxyList = new ArrayList<>();
    proxyList.add(new InetSocketAddress("github.com", 0));

    startLocalServerTunnel(PROXY_SERVER_PORT, IpConstants.LOCAL_IP_ALL);
    startClientTunnel(PROXY_SERVER_PORT,proxyList);

目前已经测试了tcp流量转发，其他功能可能尚存在bug，如果你遇到问题或想提交改进，请提交issues<br>
The tcp divert was tested, the others mabye have bug,if you have questions,please put issues.<br>

相关链接：<br>
Other link:<br>
[windivert](https://github.com/basil00/Divert)
[jdivert](https://github.com/ffalcinelli/jdivert)


