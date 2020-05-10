package com.example.playyourway;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Request extends Fragment {

    private View RequestView;
    private DatabaseReference userRef,ReqRec,FriendRef,ReqSent,ReqReceived;
    private FirebaseAuth mAuth;
    String currentuserID;
    private RecyclerView postList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.request, container, false);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        ReqSent = FirebaseDatabase.getInstance().getReference().child("FriendRequestsSent");
        currentuserID =  mAuth.getCurrentUser().getUid();
        ReqRec = FirebaseDatabase.getInstance().getReference().child("FriendRequestsReceived").child(currentuserID);
        ReqReceived = FirebaseDatabase.getInstance().getReference().child("FriendRequestsReceived");
        postList = (RecyclerView)rootView.findViewById(R.id.search_result_list);

        postList.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

                FirebaseRecyclerOptions options =
                        new FirebaseRecyclerOptions.Builder<FindFriends>()
                                .setQuery(ReqRec, FindFriends.class)
                                .build();
                FirebaseRecyclerAdapter<FindFriends,Request.RequestView> adapter = new FirebaseRecyclerAdapter<FindFriends, Request.RequestView>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final Request.RequestView holder, int position, @NonNull FindFriends model) {
                        final String userIDs = getRef(position).getKey();
                        userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String fn = dataSnapshot.child("firstname").getValue().toString();
                                String ln = dataSnapshot.child("lastname").getValue().toString();
                                String image = dataSnapshot.child("profileimage").getValue().toString();

                                Picasso.get().load(image).into(holder.userimage);

                                holder.firstname.setText(fn+" "+ln);
                                holder.Accept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        FriendRef.child(userIDs).child(currentuserID).child("status").setValue("Friend").
                                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FriendRef.child(currentuserID).child(userIDs).child("status").setValue("Friend").
                                                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){



                                                                                ReqReceived.child(currentuserID).child(userIDs)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                               if(task.isSuccessful()){
                                                                                                   ReqSent.child(userIDs).child(currentuserID)
                                                                                                           .removeValue()
                                                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                               @Override
                                                                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                                                                   if(task.isSuccessful()){
                                                                                                                       Toast.makeText(getActivity(),"Friend Request Accepted",Toast.LENGTH_SHORT).show();
                                                                                                                   }
                                                                                                               }
                                                                                                           });
                                                                                               }
                                                                                            }
                                                                                        });

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                    }
                                });
                                holder.Decline.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        ReqReceived.child(currentuserID).child(userIDs)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            ReqSent.child(userIDs).child(currentuserID)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                Toast.makeText(getActivity(),"Friend Request Declined",Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                    }
                                });


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                ReqRec.child(userIDs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public RequestView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.showfriendrequeststile,viewGroup,false);
                        RequestView viewHolder = new RequestView(view);
                        return viewHolder;


                    }
                };
                postList.setAdapter(adapter);
                adapter.startListening();

    }

    public static class RequestView extends RecyclerView.ViewHolder{

        TextView firstname;
        ImageView Accept,Decline;
        CircleImageView userimage;

        public RequestView(@NonNull View itemView) {
            super(itemView);
            firstname = itemView.findViewById(R.id.all_users_profile_full_name);
            Accept = itemView.findViewById(R.id.acceptrequest);
            Decline = itemView.findViewById(R.id.declinerequest);
            userimage = itemView.findViewById(R.id.all_users_profile_image);
        }
    }
}
