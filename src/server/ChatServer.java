package server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Vector;

import client.ChatClient3IF;

/**
 * 
 * @author Edson e Jorge
 *
 */
public class ChatServer extends UnicastRemoteObject implements ChatServerIF {
	String line = "---------------------------------------------\n";
	private Vector<Chatter> chatters;
	private static final long serialVersionUID = 1L;
        
	public ChatServer() throws RemoteException {
		super();
		chatters = new Vector<Chatter>(10, 1);
	}
	
	//-----------------------------------------------------------
	/**
	 *  MÉTODOS LOCAIS
	 */	
	public static void main(String[] args) {
		startRMIRegistry();	
		String hostName = "localhost";
		String serviceName = "GroupChatService";
		
		if(args.length == 2){
			hostName = args[0];
			serviceName = args[1];
		}
		
		try{
			ChatServerIF hello = new ChatServer();
			Naming.rebind("rmi://" + hostName + "/" + serviceName, hello);
			System.out.println("Group Chat RMI Server is running...");
		}
		catch(Exception e){
			System.out.println("Server had problems starting");
		}	
	}

	
	/**
	 *  Inicia o RMI de registro
	 */
	public static void startRMIRegistry() {
		try{
			java.rmi.registry.LocateRegistry.createRegistry(1099);
			System.out.println("RMI Server ready");
		}
		catch(RemoteException e) {
			e.printStackTrace();
		}
	}
		
	
	//-----------------------------------------------------------
	/*
	 *   MÉTODOS REMOTOS
	 */
	
	/**
	 * Mostra mensagem ao client
	 */
	public String sayHello(String ClientName) throws RemoteException {
		System.out.println(ClientName + " sent a message");
		return "Hello " + ClientName + " from group chat server";
	}
	

	/**
	 * Envia uma string para todos os clients conectados
	 */
	public void updateChat(String name, String nextPost) throws RemoteException {
		String message =  name + " : " + nextPost + "\n";
		sendToAll(message);
	}
	
	/**
	 * Recebe uma nova referência de cliente remoto
	 */
	@Override
	public void passIDentity(RemoteRef ref) throws RemoteException {
		try{
			System.out.println(line + ref.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	/**
	 * Recebe um novo client e mostra detalhes no console
	 * 
	 */
	@Override
	public void registerListener(String[] details) throws RemoteException {	
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println(details[0] + " has joined the chat session");
		System.out.println(details[0] + "'s hostname : " + details[1]);
		System.out.println(details[0] + "'sRMI service : " + details[2]);
		registerChatter(details);
	}

	
	/**
	 * registra a interface do cliente e a armazena em uma referência para
         * mensagens que serão enviadas, ou seja, mensagens de outros membros da sessão de chat.
	 * @param details
	 */
	private void registerChatter(String[] details){		
		try{
			ChatClient3IF nextClient = ( ChatClient3IF )Naming.lookup("rmi://" + details[1] + "/" + details[2]);
			
			chatters.addElement(new Chatter(details[0], nextClient));
			
			nextClient.messageFromServer("[Server] : Olá " + details[0] + " você está livre para conversar.\n");
			
			sendToAll("[Server] : " + details[0] + " entrou para o grupo.\n");
			
			updateUserList();		
		}
		catch(RemoteException | MalformedURLException | NotBoundException e){
			e.printStackTrace();
		}
	}
	
	/**
         * Atualiza todos os clientes invocando remotamente seus
         * métodos RMI updateUserList
	 */
	private void updateUserList() {
		String[] currentUsers = getUserList();	
		for(Chatter c : chatters){
			try {
				c.getClient().updateUserList(currentUsers);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}
	

	/**
	 * Gera um array de strings de usuários registrados no momento
	 * @return
	 */
	private String[] getUserList(){
		// generate an array of current users
		String[] allUsers = new String[chatters.size()];
		for(int i = 0; i< allUsers.length; i++){
			allUsers[i] = chatters.elementAt(i).getName();
		}
		return allUsers;
	}
	

	/**
	 * Envia mensagem para todos os usuários
	 * @param newMessage
	 */
	public void sendToAll(String newMessage){	
		for(Chatter c : chatters){
			try {
				c.getClient().messageFromServer(newMessage);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}

	
	/**
	 * remove um client da lista, notifica todos os participantes
	 */
	@Override
	public void leaveChat(String userName) throws RemoteException{
		
		for(Chatter c : chatters){
			if(c.getName().equals(userName)){
				System.out.println(line + userName + " saiu do chat");
				System.out.println(new Date(System.currentTimeMillis()));
				chatters.remove(c);
				break;
			}
		}		
		if(!chatters.isEmpty()){
			updateUserList();
		}			
	}
	

	/**
	 * Um método para enviar uma mensagem privada para o client selecionado
	 */
	@Override
	public void sendPM(int[] privateGroup, String privateMessage) throws RemoteException{
		Chatter pc;
		for(int i : privateGroup){
			pc= chatters.elementAt(i);
			pc.getClient().messageFromServer(privateMessage);
		}
	}
	
}



