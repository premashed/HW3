package com.example;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TreesDAO {

    public static List<Tree> readTable(Session session) {
        List<Tree> treeList = new ArrayList<>();
        Transaction readTransaction = session.beginTransaction();
        List<TreeEntity> EntityPairs = session.createQuery("FROM TreeEntity", TreeEntity.class).getResultList();
        readTransaction.commit();
        for (TreeEntity pair : EntityPairs) {
            int nodeId = pair.getId();
            int parentNodeId = pair.getParentId();
            Node node = new Node(nodeId);
            if (nodeId == parentNodeId) {
                node.setParent(null); // создание нового дерева из корня
                Tree Newtree = new Tree(node);
                treeList.add(Newtree);
            } else {
                for (Tree tree : treeList) {
                    Node rootNode = tree.getRoot();
                    Node parent = findNodeById(rootNode, parentNodeId); // поиск родителя в каждом дереве
                    if (parent != null) {
                        node.setParent(parent); // добавление при совпадении
                        parent.addChild(node);
                    }
                }
            }
        }
        return treeList;
    }

    private static Node findNodeById(Node node, int nodeId) {
        if (node.getId() == nodeId) {
            return node;
        }
        for (Node child : node.getChildren()) {
            Node result = findNodeById(child, nodeId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static void writeTable(Session session, List<Tree> trees) {
        Transaction cleanTransaction = session.beginTransaction();
        session.createNativeQuery("TRUNCATE TABLE Trees", TreeEntity.class).executeUpdate();
        cleanTransaction.commit();
        Transaction write = session.beginTransaction();
        session.clear();
        for (Tree tree : trees) {
            for (Node node : tree.getAllNodes()) {
                int parentId = node.getId();
                if (!node.isRoot()) {
                    parentId = node.getParent().getId();
                }
                session.persist(new TreeEntity(node.getId(), parentId));
            }
        }
        write.commit();
    }

    public static void insert(Session session, int[] li) {
        Transaction insertTransaction = session.beginTransaction();
        for (int i = 0; i < li.length; i += 2) {
            session.persist(new TreeEntity(li[i], li[i + 1]));
        }
        insertTransaction.commit();
    }
}

