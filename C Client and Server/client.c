/*
Programmer: Kyle Shoop
Professor: Dr. Girard
Class: Computer networks
file: client.c
Date: 4 April 2017
*/

#include <sys/types.h>
#include <string.h>
#include <stdint.h>
#include <inttypes.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <stdio.h>
#include <signal.h>
#include <unistd.h>
#include <netdb.h>



///start here...... need to make client able to recieve messages from router






uint8_t checksum ( uint8_t array[5])
{
        uint8_t sum = 0x00;
        int temp;

        for(temp = 0; temp < 5; temp ++)
        {
		if(temp != 2)
                	sum = sum + array[temp];
        }

        return sum;
}




int main()
{

	int sockfd, m_count, exit = 0;
	
	uint8_t data = 0x01;

	uint8_t sendline[5];
	uint8_t recvline[5];
	struct sockaddr_in routeraddr;

	int client_num = 0;

	printf("\nPlease enter client number to initialize: ");
	scanf("%d", &client_num);
	printf("\n\n");


	signal(SIGPIPE, SIG_IGN);

	sockfd = socket (AF_INET, SOCK_STREAM, 0);
	bzero (&routeraddr, sizeof (routeraddr));	

	routeraddr.sin_family = AF_INET;
	routeraddr.sin_port = htons(4446);

	inet_pton(AF_INET,"127.0.0.1", &(routeraddr.sin_addr));

	connect(sockfd, (struct sockaddr *)&routeraddr, sizeof(routeraddr));

		bzero(sendline, 5);
		bzero(recvline, 5);

		//byte 1, MESSAGE SOURCE
		sendline[0] = (uint8_t) client_num;

		//byte 2, MESSAGE DESTINATION
		sendline[1] = 0x01;

		//byte 4, DATA byte 1
		sendline[3] = data;

		//byte 5, DATA byte 2
		sendline[4] = data;

		//byte 3, 8-bit CHECKSUM
    sendline[2] = checksum(sendline);

		
		for(m_count = 0 ; m_count < 5; m_count ++)
		{
			printf("(client) byte %d = " "%" PRIu8 "\n" ,m_count, sendline[m_count]);
		}
		printf("\n");

		exit = write(sockfd, sendline, sizeof(sendline)+1);

		if(exit < 0)
		{
			printf("\n(client) ERROR, unable to write to router.\n*Program Terminated*\n\n");
			return 0;
		}
     else
     {
       printf("write successful.\n\n");
     }
    /*
		exit = read(sockfd, recvline, sizeof(recvline)+1);

                if(exit < 0)
                {
                        printf("\n(client) ERROR, unable to read from router.\n*Program Terminated*\n\n");
                        break;
                }


		 for(m_count = 0 ; m_count < 5; m_count ++)
                {
			printf("\n(client) output from server: " "%" PRIu8 "\n", recvline[m_count]);
		}
    */
    
	return 0;

}
