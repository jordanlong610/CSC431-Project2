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

//change IP addresses depending on connections
#define CLIENT_IP "127.0.0.1"
#define ROUTER1_IP "127.0.0.1"
#define ROUTER2_IP "127.0.0.1"
#define ROUTER3_IP "127.0.0.1"
#define ROUTER4_IP "127.0.0.1"
			

//structure for passing data to threads
struct data{
	long socket;
	int server_number;
} to_thread, *thread_data;

uint8_t message[5];

struct lookup{
	int neighbor1_num, neighbor2_num;
	char neighbor1_ip, neighbor2_ip;
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

	char send_ip[9];
  int sockfd;
  int exit;
  struct sockaddr_in addr;

	//check to determine where to send the received message
	//		
	//
	//vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

	if (thread_data->server_number == message[1])
	{
		//send_ip = CLIENT_IP;
    strcpy(send_ip, CLIENT_IP);
		
	}
	else 
	{
		if(message[1] == table.neighbor1_num)
		{
			//send_ip = table.neighbor1_ip;
      strcpy(send_ip, &table.neighbor1_ip);
		}
		else if(message[1] == table.neighbor2_num)
		{
			//send_ip = table.neighbor2_ip;
      strcpy(send_ip, &table.neighbor2_ip);
		}
		else
		{
			//send_ip = table.neighbor1_ip;
      strcpy(send_ip, &table.neighbor1_ip); 
		}
	}

	//send message
	sockfd = socket (AF_INET, SOCK_STREAM, 0);
	bzero (&addr, sizeof (addr));	

	addr.sin_family = AF_INET;
	addr.sin_port = htons(4446);

	inet_pton(AF_INET, send_ip, &(addr.sin_addr));

	connect(sockfd, (struct sockaddr *)&addr, sizeof(addr));

	printf("connected to client\n\n");

	exit = write(sockfd, message, sizeof(message)+1);

	if(exit < 0 ) 
	{
		printf("(router) Send to client failed.");
	}

	pthread_exit(NULL);
}

void* receive(void* argp)
{
	//set thread_data structure pointer equal to void* pointer passed to thread
	thread_data = (struct data *) argp;

	printf("Router %d Created\n\n", thread_data->server_number);

		long connection_socket;		
		
		int temp, exit = 0;
		

		//set variable to socket connection		
		 connection_socket = thread_data->socket;


		//zero the message variable
		bzero(message, sizeof(message));
	
		//read message from client
		exit = read(connection_socket, message, sizeof(message)+1);

		//check for read error
		if( exit < 0 )
		{
			printf("\n(router) ERROR, unable to read from client.\n*Thread Terminated*\n\n");
			pthread_exit(NULL);
		}

		//perform checksum error detection
		if(message[2] == checksum(message))
		{
			//message must have a source, if not there was an error
			if(message[0] == 0)
			{
				printf("\n(router) ERROR, unable to read from client.\n*Thread Terminated*\n\n");
				pthread_exit(NULL);
			}
			

			//print the message received by the router
			for(temp = 0; temp < 5; temp ++)
               		{
                        	printf("(router) message received byte %d = " "%" PRIu8 "\n", temp, message[temp]);
               		}

			
					
			//send message to client
			int error = 0;
			pthread_t send_thread;

      printf("creating send thread");
			error = pthread_create(&send_thread,NULL, send_func, (void *)&to_thread);
		
			
			if(error)
			{

				printf("ERROR #%d, program terminated", error);
				pthread_exit(NULL);

			}

			//wait for send thread to finish and close
			pthread_join(send_thread,NULL);
			printf("\nSend thread exited successfully\n");
								
		}
		//checksum failed
		else
		{
			printf("\n(router) ERROR, Checksum failed\n message ignored\n\n");
		
		}

	pthread_exit(NULL);
}



int main()
{

	pthread_t receive_thread;	

	long error;

	int server_num;

	int send_flag;
	
	signal(SIGPIPE, SIG_IGN);

	long listen_socket, comm_socket;

	struct sockaddr_in router_info;

	printf("\nPlease enter router number to initialize (must be same as connecting client): ");
	scanf("%d", &server_num);
	printf("\n\n");
 
	//save server number
	to_thread.server_number = server_num;

	//build lookup table with neighboring router numbers
	switch(server_num){
		case 1: 
			table.neighbor1_num = 2;
			table.neighbor2_num = 4;
			table.neighbor1_ip = *ROUTER2_IP;
			table.neighbor2_ip = *ROUTER4_IP;
			break;
		case 2:
			table.neighbor1_num = 1;
			table.neighbor2_num = 3;
			table.neighbor1_ip = *ROUTER1_IP;
			table.neighbor2_ip = *ROUTER3_IP;
			break;
		case 3: 
			table.neighbor1_num = 2;
			table.neighbor2_num = 4;
			table.neighbor1_ip = *ROUTER2_IP;
			table.neighbor2_ip = *ROUTER4_IP;
			break;
		case 4:
			table.neighbor1_num = 1;
			table.neighbor2_num = 3;
			table.neighbor1_ip = *ROUTER1_IP;
			table.neighbor2_ip = *ROUTER3_IP;
			break;
	}

	listen_socket = socket (AF_INET, SOCK_STREAM, 0);

	bzero(&router_info, sizeof(router_info));

	router_info.sin_family = AF_INET;
	router_info.sin_addr.s_addr = htons(INADDR_ANY);
	router_info.sin_port = htons(4446);

	bind(listen_socket,(struct sockaddr*)&router_info, sizeof(router_info));

  printf("router waiting for connection...\n\n");
  
  //listen for connections
	listen(listen_socket, 10);
	
	while(1)
	{	
		//create thread for receive socket accept
		comm_socket = accept(listen_socket, (struct sockaddr*) NULL, NULL);

		to_thread.socket = comm_socket;

		printf("\ncreating receive thread\n");

		error = pthread_create(&receive_thread,NULL, receive, (void *)&to_thread);
		
		if(error)
		{

			printf("ERROR #%ld, program terminated", error);
			exit(-1);

		}

		//wait for receive thread to finish and close
		pthread_join(receive_thread,NULL);
		printf("\nReceive thread exited successfully\n");

	}
	


	pthread_exit(NULL);
}

