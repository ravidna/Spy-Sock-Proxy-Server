# Spysock
Spying Socks Proxy server implementation 
Create an application which starts a SOCKS v4 Proxy, listening on port 8080 and supporting (exactly) 20 concurrent connections.
The program will start the proxy server, which will continue running until it is interrupted by CTRL+C (i.e., SIGINT signal).
The program should look for passwords passed using HTTP Basic Authentication, only in connections where the destination is with port 80, and the HTTP method is GET.
