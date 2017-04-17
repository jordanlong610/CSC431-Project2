/*
Programmer: Kyle Shoop
Professor: Dr. Girard
Class: Computer networks
file: pserver2.c 
Date: 4 April 2017
*/

#include <stdio.h>
#include <unistd.h>
#include <inttypes.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netdb.h>
#include <signal.h>
#include <string.h>
#include <sys/types.h>
#include <pthread.h>
#include <stdint.h>
#include <arpa/inet.h>
			

//structure for passing data to threads
struct data{
	long socket;
	int server_number;
}thread_data, *to_thread;

uint8_t message[5];

struct lookup{
	int self_num,neighbor1_num, neighbor2_num;
	char self_ip[20], client_ip[20], neighbor1_ip[20], neighbor2_ip[20];
}table;

//checksum function
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

void* send_func(void* argp)
{

	printf("Router send thread created\n\n");

	char send_ip[20];
	char recvline[50];
  int sockfd;
  int exit;
  struct sockaddr_in addr;

	//check to determine where to send the received message
	//		
	//
	//vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

	if (table.self_num == message[1])
	{
		//send_ip = CLIENT_IP;
    		strcpy(send_ip, table.client_ip);
		
	}
	else 
	{
		if(message[1] == table.neighbor1_num)
		{
			//send_ip = table.neighbor1_ip;
      			strcpy(send_ip, table.neighbor1_ip);
		}
		else if(message[1] == table.neighbor2_num)
		{
			//send_ip = table.neighbor2_ip;
      			strcpy(send_ip, table.neighbor2_ip);
		}
		else
		{
			//send_ip = table.neighbor1_ip;
      			strcpy(send_ip, table.neighbor1_ip); 
		}
	}

	//send message
	sockfd = socket (AF_INET, SOCK_STREAM, 0);
	bzero (&addr, sizeof (addr));	

	addr.sin_family = AF_INET;
	addr.sin_port = htons(8080);

	//addr.sin_addr.s_addr = inet_addr(send_ip);
	inet_pton(AF_INET, send_ip, &(addr.sin_addr));

	connect(sockfd, (struct sockaddr *)&addr, sizeof(addr));

	printf("(router) connected to client \n\n");

	exit = write(sockfd, message, sizeof(message)+1);

	if(exit < 0 ) 
	{
		printf("(router) Send to %s destination failed.\n\n",send_ip);
	}
	
	//read the receive acknowledgement from the router
	exit = read(sockfd, (void *)recvline, sizeof(recvline)+1);

	if(exit < 0)
	{
		printf("\n(client) ERROR, unable to read from router.\n*Program Terminated*\n\n");
		pthread_exit(NULL);
	}


	printf("\n(router) output from destination: %s\n\n", recvline);

	pthread_exit(NULL);
}

void* receive(void * argp )
{
	

		printf("Router %d receive thread created.\n\n", table.self_num);

		long connection_socket;		
		
		int temp, exit = 0;
		

		//set variable to socket connection		
		 connection_socket = thread_data.socket;


		//zero the message variable
		bzero(message, sizeof(message));
	
		//read message from client
		exit = read(connection_socket, message, sizeof(message)+1);

		//check for read error
		if( exit < 0 )
		{
			printf("\n(router) ERROR, unable to read from router.\n*Thread Terminated*\n\n");
			pthread_exit(NULL);
		}

		//perform checksum error detection
		if(message[2] == checksum(message))
		{
			//message must have a source, if not there was an error
			if(message[0] == 0)
			{
				printf("\n(router) ERROR, unable to read from router.\n*Thread Terminated*\n\n");
				pthread_exit(NULL);
			}
			

			//print the message received by the router
			for(temp = 0; temp < 5; temp ++)
               		{
                        	printf("(router %d) message received byte %d = " "%" PRIu8 ".\n", table.self_num, temp, message[temp]);
               		}
			
			//send an acknowledgement to the sender that the router received the message
			char receive_ack[50] = "Communication Successful";

			exit = write(connection_socket, receive_ack, sizeof(receive_ack)+1);

			if(exit < 0)
			{
				printf("\n(client) ERROR, unable to write to router.\n*Program Terminated*\n\n");
				return 0;
			}
			
					
			//send message to client
			int error = 0;
			pthread_t send_thread;

     			 printf("creating router send thread\n\n");

			error = pthread_create(&send_thread,NULL, send_func, (void *)to_thread);
		
			
			if(error)
			{

				printf("ERROR #%d, program terminated\n\n", error);
				pthread_exit(NULL);

			}
					
			//wait for send thread to finish and close
			pthread_join(send_thread,NULL);
			printf("\nSend thread exited successfully.\n\n");
						
		}
		//checksum failed
		else
		{
			printf("\n(router) ERROR, Checksum failed.\n message ignored.\n\n");
		
		}

	pthread_exit(NULL);
}



int main()
{

	pthread_t receive_thread;	

	long error;

	int server_num;

	char client_ip[20], router1_ip[20], router2_ip[20], router3_ip[20], router4_ip[20];

	int send_flag;
	
	signal(SIGPIPE, SIG_IGN);

	long listen_socket, comm_socket;

	struct sockaddr_in router_info;

	printf("\nPlease enter router number to initialize (must be same as connecting client): ");
	scanf("%d", &server_num);
	printf("\n\n");

	printf("\nPlease enter ip number for client: ");
	scanf("%s", client_ip);
	printf("\n\n");

	strcpy(table.client_ip, client_ip);


	printf("\nPlease enter ip number for router #1: ");
	scanf("%s", router1_ip);
	printf("\n\n");

	printf("\nPlease enter ip number for router #2: ");
	scanf("%s", router2_ip);
	printf("\n\n");

	printf("\nPlease enter ip number for router #3: ");
	scanf("%s", router3_ip);
	printf("\n\n");

	printf("\nPlease enter ip number for router #4: ");
	scanf("%s", router4_ip);
	printf("\n\n");


	//save server number
	thread_data.server_number = server_num;

	//build lookup table with neighboring router numbers
	switch(server_num){
		case 1: 
			table.self_num = 1;
			table.neighbor1_num = 2;
			table.neighbor2_num = 4;
			strcpy(table.self_ip, router1_ip);
			strcpy(table.neighbor1_ip, router2_ip);
			strcpy(table.neighbor2_ip, router4_ip);
			break;
		case 2:
			table.self_num = 2;
			table.neighbor1_num = 1;
			table.neighbor2_num = 3;
			strcpy(table.self_ip, router2_ip);
			strcpy(table.neighbor1_ip, router1_ip);
			strcpy(table.neighbor2_ip, router3_ip);
			break;
		case 3: 
			table.self_num = 3;
			table.neighbor1_num = 2;
			table.neighbor2_num = 4;
			strcpy(table.self_ip, router3_ip);
			strcpy(table.neighbor1_ip, router2_ip);
			strcpy(table.neighbor2_ip, router4_ip);
			break;
		case 4:
			table.self_num = 4;
			table.neighbor1_num = 1;
			table.neighbor2_num = 3;
			strcpy(table.self_ip, router4_ip);
			strcpy(table.neighbor1_ip, router1_ip);
			strcpy(table.neighbor2_ip, router3_ip);
			break;
	}

	
	
	while(1)
	{	

		listen_socket = socket (AF_INET, SOCK_STREAM, 0);
		
		if (setsockopt(listen_socket, SOL_SOCKET, SO_REUSEADDR, &(int){ 1 }, sizeof(int)) < 0)
   			printf("setsockopt(SO_REUSEADDR) failed\n\n");

		bzero(&router_info, sizeof(router_info));

		router_info.sin_family = AF_INET;
		router_info.sin_addr.s_addr = htons(INADDR_ANY);
		router_info.sin_port = htons(8080);

		bind(listen_socket,(struct sockaddr*)&router_info, sizeof(router_info)); 

		printf("\n\nrouter waiting for connection...\n\n");
		//listen for connections
		listen(listen_socket, 10);
		
		//create thread for receive socket accept
		comm_socket = accept(listen_socket, (struct sockaddr*) NULL, NULL);

		thread_data.socket = comm_socket;

		printf("\ncreating router receive thread.\n");

		error = pthread_create(&receive_thread,NULL, receive,(void *)to_thread);
		
		if(error)
		{

			printf("ERROR #%ld, program terminated.\n\n", error);
			break;

		}

		//wait for receive thread to finish and close
		pthread_join(receive_thread,NULL);
		printf("\nReceive thread exited successfully.\n\n");
		
		//close the connection on listen socket
		close(listen_socket);
		listen_socket = 0;

	}
	


	return 0;
}

