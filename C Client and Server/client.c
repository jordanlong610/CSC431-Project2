/*
Programmer: Kyle Shoop
Professor: Dr. Girard
Class: Computer networks
file: client.c
Date: 1 Feb. 2017
*/

#include <sys/types.h>
#include <string.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <stdio.h>
#include <unistd.h>
#include <netdb.h>

int main()
{

	int sockfd, m_count = 1;
	char sendline[100];
	char recvline[100];
	struct sockaddr_in servaddr;

	sockfd = socket (AF_INET, SOCK_STREAM, 0);
	bzero (&servaddr, sizeof (servaddr));	

	servaddr.sin_family = AF_INET;
	servaddr.sin_port = htons(4446);

	inet_pton(AF_INET,"127.0.0.1", &(servaddr.sin_addr));

	connect(sockfd, (struct sockaddr *)&servaddr, sizeof(servaddr));

	while(1)
	{

		bzero(sendline, 100);
		bzero(recvline, 100);

		printf("\n(client) input %d: ", m_count);

		fgets(sendline, 100, stdin);

		write(sockfd, sendline, strlen(sendline)+1);
		read(sockfd, recvline, 100);
		printf("\n(client) output from server: %s", recvline);
		m_count++;

	}	
	return 0;

}
