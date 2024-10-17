package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Connection;
import TalentHunt.TalentHunt.repository.ConnectionRepository;
import TalentHunt.TalentHunt.repository.FollowRepository;
import TalentHunt.TalentHunt.repository.UserRepository;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConnectionService {
    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private UserRepository userRepository;

    private final NotificationService notificationService;

    @Autowired
    private FollowRepository followRepository;

    public ConnectionService(@Lazy NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Handles sending a connection request
    public String connect(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElse(null);
        User user2 = userRepository.findById(userId2).orElse(null);

        if (user1 == null || user2 == null) {
            return "Failed";
        }

        Connection existingConnection = connectionRepository.findByUser1AndUser2(user1, user2);
        if (existingConnection != null) {
            if ("pending".equals(existingConnection.getStatus())) {
                return "Pending"; // Already pending
            } else {
                return "Already Connected";
            }
        }

        Connection connection = new Connection();
        connection.setUser1(user1);
        connection.setUser2(user2);
        connection.setStatus("pending");
        connectionRepository.save(connection);

        return "Request sent";
    }

    // Handles withdrawing a connection request
    public String withdraw(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElse(null);
        User user2 = userRepository.findById(userId2).orElse(null);

        if (user1 == null || user2 == null) {
            return "Failed";
        }

        Connection existingConnection = connectionRepository.findByUser1AndUser2(user1, user2);
        if (existingConnection != null && "pending".equals(existingConnection.getStatus())) {
            connectionRepository.delete(existingConnection);
            return "Withdrawn";
        }

        return "Not Found";
    }

    public Connection getConnectionStatus(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElse(null);
        User user2 = userRepository.findById(userId2).orElse(null);

        if (user1 == null || user2 == null) {
            return null;
        }

        return connectionRepository.findByUser1AndUser2(user1, user2);
    }

    public List<Connection> getIncomingRequests(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        return connectionRepository.findByUser2AndStatus(user, "pending");
    }

    public List<Connection> getOutgoingRequests(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        return connectionRepository.findByUser1AndStatus(user, "pending");
    }

    // Accepts a connection request
    public String acceptRequest(Long requestId) {
        Connection connection = connectionRepository.findById(requestId).orElse(null);
        if (connection == null) {
            return "Failed";
        }

        connection.setStatus("accepted");
        connectionRepository.save(connection);

        User requester = connection.getUser1(); // Assuming user1 is the requester
        User currentUser = connection.getUser2(); // Assuming user2 is the accepter

        notificationService.notifyUserOfRequestAcceptance(currentUser, requester);

        return "Accepted";
    }

    // Declines a connection request
    public String declineRequest(Long requestId) {
        Connection connection = connectionRepository.findById(requestId).orElse(null);
        if (connection == null) {
            return "Failed";
        }
        connection.setStatus("decline");
        connectionRepository.save(connection);

        User requester = connection.getUser1(); // Assuming user1 is the requester
        User currentUser = connection.getUser2(); // Assuming user2 is the decliner

        notificationService.notifyUserOfRequestDecline(currentUser, requester);

        return "Declined";
    }


    public User getRequesterByRequestId(Long requestId) {
        Connection connection = connectionRepository.findById(requestId).orElse(null);
        if (connection == null) {
            return null;
        }

        // Assuming the requester is user1 if the status is 'pending'
        return connection.getUser1();
    }

    public int getConnectionCount(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return 0;
        }
        List<Connection> connections = connectionRepository.findByUser1AndStatus(user, "accepted");
        connections.addAll(connectionRepository.findByUser2AndStatus(user, "accepted"));
        return connections.size();
    }

    public List<Connection> getAcceptedConnections(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        List<Connection> connectionsAsUser1 = connectionRepository.findByUser1AndStatus(user, "accepted");
        List<Connection> connectionsAsUser2 = connectionRepository.findByUser2AndStatus(user, "accepted");

        connectionsAsUser1.addAll(connectionsAsUser2);

        return connectionsAsUser1;
    }

    public String removeConnection(Long userId, Long connectionId) {
        int removedCount = connectionRepository.removeConnection(userId, connectionId);
        if (removedCount > 0) {
            return "Removed";
        } else {
            return "Failed";
        }
    }

    public List<Connection> getConnectionsForUser(Long userId) {
        return connectionRepository.findConnectionsByUserId(userId);
    }

    public List<Long> getFollowingUserIds(Long userId) {
        return followRepository.findFollowingByUserId(userId).stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
    }


    // Fetch connections of a given user
    public List<User> getConnectionsOfUser(User user) {
        List<Connection> connectionsAsUser1 = connectionRepository.findByUser1AndStatus(user, "ACCEPTED");
        List<Connection> connectionsAsUser2 = connectionRepository.findByUser2AndStatus(user, "ACCEPTED");

        // Combine both lists into a single list of users
        List<User> connections = connectionsAsUser1.stream()
                .map(Connection::getUser2) // Get the connected user from user1's connections
                .collect(Collectors.toList());

        connections.addAll(connectionsAsUser2.stream()
                .map(Connection::getUser1) // Get the connected user from user2's connections
                .collect(Collectors.toList()));

        return connections;
    }

}
