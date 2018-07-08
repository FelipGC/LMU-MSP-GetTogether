package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.IService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.IServiceBinder;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.IMessageDistributionService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.JsonMessageDistributionService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.MessageDistributionBinder;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.PresentationViewModel;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.ShowViewObserver;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public abstract class AbstractPresentationFragment extends Fragment {
    private final ServiceConnection messageDistributorConnection =
            new MessageDistributorConnection();
    private final ServiceConnection connectionServiceConnection = new ConnectionServiceConnection();

    protected View startPresentationButton;
    protected View stopPresentationButton;
    protected ImageView pdfView;
    protected PresentationViewModel model;
    protected AppLogicActivity context;
    protected IMessageDistributionService messageDistributionService = null;
    protected IService connectionService = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppLogicActivity) {
            this.context = (AppLogicActivity) context;
        } else {
            throw new RuntimeException(context.toString()
                    + "must implement AppContext");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        int fragment_layout_id = getFragmentLayoutId();
        View view = inflater.inflate(fragment_layout_id, container, false);
        bindServices();
        getComponentsFrom(view);
        initButtons(view);
        initModel();
        addStateListeners();
        return view;
    }

    private void bindServices() {
        bindService(JsonMessageDistributionService.class, messageDistributorConnection);
        bindService(getServiceClass(), connectionServiceConnection);
    }

    protected abstract Class<? extends IService> getServiceClass();

    private void initModel() {
        model = ViewModelProviders.of(this).get(PresentationViewModel.class);
    }

    protected void bindService(Class<?> serviceType, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, serviceType);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void addStateListeners() {
        model.getActivePage().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                pdfView.setImageBitmap(bitmap);
            }
        });
        model.getMessage().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer id) {
                if (id == null) {
                    return;
                }
                context.displayShortMessage(getString(id));
            }
        });
        model.getShowStartButton().observe(this,
                new ShowViewObserver(startPresentationButton));
        model.getShowStopButton().observe(this,
                new ShowViewObserver(stopPresentationButton));
    }

    protected abstract void initButtons(View view);

    private void getComponentsFrom(View view) {
        startPresentationButton = view.findViewById(R.id.presentation_startPresentationButton);
        stopPresentationButton = view.findViewById(R.id.presentation_stopPresentationButton);
        pdfView = view.findViewById(R.id.presentation_pdfView);
    }

    public abstract int getFragmentLayoutId();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context.unbindService(messageDistributorConnection);
        context.unbindService(connectionServiceConnection);
    }

    private class MessageDistributorConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MessageDistributionBinder binder = (MessageDistributionBinder) service;
            messageDistributionService = binder.getService();
            onMessageDistributionServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            messageDistributionService = null;
        }
    }

    protected void onMessageDistributionServiceConnected() {
    }

    private class ConnectionServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IServiceBinder binder = (IServiceBinder) service;
            connectionService = binder.getService();
            onConnectionServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectionService = null;
        }
    }

    protected void onConnectionServiceConnected() {
    }
}
