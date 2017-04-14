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
#include <time.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <stdio.h>
#include <signal.h>
#include <pthread.h>
#include <unistd.h>
#include <netdb.h>

//global variable for client number
//this will not create race conditions
//it is only being written once before it is read
unsigned int client_num = 0;


//checksum algorithm
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

//thread to receive messages that have been sent from another client
void * receive_func(void * argp)
{
	int exit, temp;

	uint8_t message[5];
	long error;
	struct sockaddr_in router_info;
	long listen_socket, comm_socket;

	while(1)
	{	

		bzero(&listen_socket, sizeof(listen_socket));
		bzero(&comm_socket, sizeof(comm_socket));

		printf("client receive thread created\nclient waiting for connection...\n\n");
		
		listen_socket = socket (AF_INET, SOCK_STREAM, 0);
		
		if (setsockopt(listen_socket, SOL_SOCKET, SO_REUSEADDR, &(int){ 1 }, sizeof(int)) < 0)
   			printf("setsockopt(SO_REUSEADDR) failed\n\n");

		bzero(&router_info, sizeof(router_info));

		router_info.sin_family = AF_INET;
		router_info.sin_addr.s_addr = htons(INADDR_ANY);
		router_info.sin_port = htons(4447);

		bind(listen_socket,(struct sockaddr*)&router_info, sizeof(router_info)); 

		printf("\n\nrouter waiting for connection...\n\n");

		//listen for connections
		listen(listen_socket, 10);
		
		//socket accept
		comm_socket = accept(listen_socket, (struct sockaddr*) NULL, NULL);

		exit = read(comm_socket, message, sizeof(message)+1);

		//check for read error
		if( exit < 0 )
		{
			printf("\n(client) ERROR, unable to read from router.\n*Thread Terminated*\n\n");
			pthread_exit(NULL);
		}

		//perform checksum error detection
		if(message[2] == checksum(message))
		{
			//message must have a source, if not there was an error
			if(message[0] == 0)
			{
				printf("\n(client) ERROR, unable to read from router.\n*Thread Terminated*\n\n");
				pthread_exit(NULL);
			}
			

			//print the message received by the router
			for(temp = 0; temp < 5; temp ++)
               		{
                        	printf("(cliet) message received byte %d = " "%" PRIu8 ".\n", temp, message[temp]);
               		}
			
			//send an acknowledgement to the sender that the router received the message
			char receive_ack[50] = "Communication Successful";

			exit = write(comm_socket, receive_ack, sizeof(receive_ack)+1);

			if(exit < 0)
			{
				printf("\n(client) ERROR, unable to write to router.\n*Program Terminated*\n\n");
				return 0;
			}
		}
		else
		{
			printf("\n(client) ERROR, Checksum failed.\n message ingnored.\n\n");
		}

		//close the connection on listen socket
		close(listen_socket);

	}

	pthread_exit(NULL);

}

//thread to send messages to other clients thorugh the server
void * send_func(void * argp)
{
	printf("client send thread created\n\n");

	int sockfd,count, exit = 0;

	uint8_t data = 0x01;
	uint8_t destination = 0x01;

	uint8_t sendline[5];
	char recvline[50];
	struct sockaddr_in routeraddr;

	while(1)
	{
		srand(time(NULL));

		destination = rand() %4 + 1;
		
		//zero out the send and receive variables
		bzero(sendline, 5);
		bzero(recvline, 50);

		bzero(&routeraddr, sizeof(routeraddr));


		//byte 1, MESSAGE SOURCE
		sendline[0] = (uint8_t) client_num;

		//byte 2, MESSAGE DESTINATION
		sendline[1] = destination;

		//byte 4, DATA byte 1
		sendline[3] = data;

		//byte 5, DATA byte 2
		sendline[4] = data;

		//byte 3, 8-bit CHECKSUM
		sendline[2] = checksum(sendline);

		//print each byte of the message that is being sent
		for(count = 0 ; count < 5; count ++)
		{
			printf("(client) byte %d = " "%" PRIu8 "\n" ,count, sendline[count]);
		}
		printf("\n");

		//surpress sigpipe signal
		signal(SIGPIPE, SIG_IGN);

		//create the socket
		sockfd = socket (AF_INET, SOCK_STREAM, 0);
		bzero (&routeraddr, sizeof (routeraddr));	

		routeraddr.sin_family = AF_INET;

		//set the socket to connect to
		routeraddr.sin_port = htons(4446);
		
		//set the ip to connect to which will the the associated router
		inet_pton(AF_INET,"127.0.0.1", &(routeraddr.sin_addr));

		//initialize connection
		connect(sockfd, (struct sockaddr *)&routeraddr, sizeof(routeraddr));

		//write the message to the router
		exit = write(sockfd, sendline, sizeof(sendline)+1);

		if(exit < 0)
		{
			printf("\n(client) ERROR, unable to write to router.\n*Program Terminated*\n\n");
			break;
		}
		else
		{
			printf("client write successful.\n\n");
		}

		//read the receive acknowledgement from the router
		exit = read(sockfd, (void *)recvline, sizeof(recvline)+1);

		if(exit < 0)
		{
			printf("\n(client) ERROR, unable to read from router.\n*Program Terminated*\n\n");
			break;
		}


		printf("\n(client) output from router: %s\n\n", recvline);

		//sleep for 2 seconds
		sleep(2);

		data++;
	}
	
	pthread_exit(NULL);
}




int main()
{

	pthread_t send_thread, receive_thread;

	

	int error = 0;

	//input the number of the client, this must be the same as the router receiving its messages
	printf("\nPlease enter client number to initialize(must be equal to receiving router): ");
	scanf("%d", &client_num);
	printf("\n\n");

	//create the thread to continuously send the message to the router
	error = pthread_create(&send_thread,NULL, send_func, (void *) &client_num);
		
	if(error)
	{

		printf("ERROR #%d, program terminated.\n\n", error);
		return -1;

	}

	//create the thread to continuously receive messages from the router
	error = pthread_create(&receive_thread,NULL, receive_func, (void *) &client_num);
		
	if(error)
	{

		printf("ERROR #%d, program terminated.\n\n", error);
		return -1;

	}

	//wait for send thread to finish and close
	pthread_join(send_thread,NULL);
	printf("\nSend thread exited successfully.\n\n");
	//wait for receive thread to finish and close
	pthread_join(receive_thread,NULL);
	printf("\nReceive thread exited successfully.\n\n");

	return 0;

}
