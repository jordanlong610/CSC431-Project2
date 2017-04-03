/*
Programmer: Kyle Shoop
Professor: Dr. Girard
Class: Computer networks
file: basic.c 
Date: 15 February 2017
*/

#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netdb.h>
#include <signal.h>
#include <string.h>
#include <sys/types.h>
#include <pthread.h>

#define NUM_THREADS 10

void* pthread(void* socket)
{
	long comm_socket, m_count;
		
	for(m_count = 0; m_count < 10; m_count++)
	{

		char str[100];
		comm_socket = (long) socket;
		
		int location, shift, length;

		bzero(str, 100);

		read(comm_socket, str, 100);

		if(str[strlen(str)-1] == '\n')
			length = strlen(str) -1;
		else
			length = strlen(str);

		printf("\n(server)reversing - %s", str);

		for(location = 0; location < length; location++)
		{

			for(shift = 0; shift < length - (location+1); shift++)
			{
			
				char temp;
				
				temp = str[shift];
				str[shift] = str[shift +1];
				str[shift+1] = temp;

			}
		}
		write(comm_socket, str, strlen(str)+1);

	}
	

	pthread_exit(NULL);
}



int main()
{

	pthread_t threads[NUM_THREADS];	

	long t_num, error;
	
	long listen_socket, comm_socket;

	struct sockaddr_in server_info;

	listen_socket = socket (AF_INET, SOCK_STREAM, 0);

	bzero(&server_info, sizeof(server_info));

	server_info.sin_family = AF_INET;
	server_info.sin_addr.s_addr = htons(INADDR_ANY);
	server_info.sin_port = htons(4446);

	bind(listen_socket,(struct sockaddr*)&server_info, sizeof(server_info));

	listen(listen_socket, 10);

	for(t_num = 0; t_num < NUM_THREADS; t_num++)
	{
		comm_socket = accept(listen_socket, (struct sockaddr*) NULL, NULL);

		printf("\ncreating thread #%ld\n",t_num);

		error = pthread_create(&threads[t_num],NULL, pthread, (void*)comm_socket);
		
		if(error)
		{

			printf("ERROR #%ld, program terminated", error);
			exit(-1);

		}

	}

	for(t_num = 0; t_num < NUM_THREADS; t_num++)
	{

		pthread_join(threads[t_num],NULL);
		printf("\nthread #%ld exited successfully\n", t_num);

	}


	pthread_exit(NULL);
}

