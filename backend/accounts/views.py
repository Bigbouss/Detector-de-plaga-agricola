from rest_framework import viewsets, status, mixins
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.views import APIView
from rest_framework.decorators import action
from rest_framework_simplejwt.views import TokenObtainPairView
from django.db import transaction
import logging

from .serializers import (
    UserSerializer, UserCreateSerializer,
    RegisterAdminSerializer, RegisterWorkerSerializer,
    MyTokenObtainPairSerializer
)
from .models import User, Role
from .permissions import IsAdmin

logger = logging.getLogger(__name__)


class UserViewSet(viewsets.ModelViewSet):
    queryset = User.objects.select_related('role', 'empresa').all()
    serializer_class = UserSerializer
    permission_classes = [IsAuthenticated]

    def get_serializer_class(self):
        if self.action == 'create':
            return UserCreateSerializer
        return UserSerializer

    def get_permissions(self):
        if self.action in ['create', 'destroy']:
            return [IsAdmin()]
        return super().get_permissions()

    def perform_destroy(self, instance):
        if instance.role and instance.role.code == 'ADMIN':
            from rest_framework.exceptions import ValidationError
            raise ValidationError("No puedes eliminar usuarios ADMIN.")
        instance.is_active = False
        instance.save()


class MyTokenObtainPairView(TokenObtainPairView):
    serializer_class = MyTokenObtainPairSerializer


class RegisterAdminView(APIView):
    permission_classes = [AllowAny]

    @transaction.atomic
    def post(self, request):
        logger.info("=" * 80)
        logger.info("REQUEST RECIBIDO en RegisterAdminView")
        logger.info(f"Content-Type: {request.content_type}")
        logger.info(f"Data recibida: {request.data}")
        logger.info("=" * 80)
        
        try:
            ser = RegisterAdminSerializer(data=request.data)
            
            if not ser.is_valid():
                logger.error(f"Errores de validación: {ser.errors}")
                return Response(ser.errors, status=status.HTTP_400_BAD_REQUEST)
            
            logger.info("Datos validados correctamente")
            user = ser.create(ser.validated_data)
            logger.info(f"Usuario creado: {user.id}")
            
            return Response({
                "message": "Admin y empresa creados",
                "user_id": user.id,
                "empresa_id": user.empresa_id
            }, status=status.HTTP_201_CREATED)
            
        except Exception as e:
            logger.exception(f"Exception en RegisterAdminView: {e}")
            return Response({
                "error": str(e)
            }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class RegisterWorkerView(APIView):
    permission_classes = [AllowAny]

    @transaction.atomic
    def post(self, request):
        logger.info("=" * 80)
        logger.info("REQUEST RECIBIDO en RegisterWorkerView")
        logger.info(f"Data recibida: {request.data}")
        logger.info("=" * 80)
        
        try:
            ser = RegisterWorkerSerializer(data=request.data)
            
            if not ser.is_valid():
                logger.error(f"Errores de validación: {ser.errors}")
                return Response(ser.errors, status=status.HTTP_400_BAD_REQUEST)
            
            user = ser.create(ser.validated_data)
            logger.info(f"Worker creado: {user.id}")
            
            return Response({
                "message": "Worker creado y unido a empresa",
                "user_id": user.id,
                "empresa_id": user.empresa_id
            }, status=status.HTTP_201_CREATED)
            
        except Exception as e:
            logger.exception(f"Exception en RegisterWorkerView: {e}")
            return Response({
                "error": str(e)
            }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
