from rest_framework import viewsets, mixins, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated, AllowAny
from django.apps import apps
from .models import JoinCode
from .serializers import JoinCodeSerializer, ValidateJoinCodeSerializer
from .permissions import IsCompanyAdmin
from django.shortcuts import get_object_or_404

class JoinCodeViewSet(
    mixins.CreateModelMixin,
    mixins.ListModelMixin,
    mixins.RetrieveModelMixin,
    viewsets.GenericViewSet,
):
    serializer_class = JoinCodeSerializer
    permission_classes = [IsAuthenticated, IsCompanyAdmin]

    def get_queryset(self):
        return JoinCode.objects.filter(empresa=self.request.user.empresa).order_by('-created_at')


class JoinCompanyView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        code = (request.data.get('join_code') or '').strip().upper()
        if not code:
            return Response({'detail': 'join_code requerido.'}, status=400)
        # buscar join code
        try:
            jc = JoinCode.objects.get(code=code)
        except JoinCode.DoesNotExist:
            return Response({'detail': 'Código inválido.'}, status=400)
        if not jc.is_valid:
            return Response({'detail': 'Código expirado o sin cupos.'}, status=400)
        try:
            user = jc.redeem_for(request.user)
        except Exception as e:
            return Response({'detail': str(e)}, status=400)
        return Response({'message': f'Te uniste a {user.empresa.name} como {user.role}.'}, status=200)


class ValidateJoinCodeView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        code = (request.data.get('code') or '').strip().upper()
        if not code:
            return Response({'valid': False, 'error': 'Código requerido.'}, status=400)
        try:
            jc = JoinCode.objects.select_related('empresa').get(code=code)
        except JoinCode.DoesNotExist:
            return Response({'valid': False, 'error': 'Código no encontrado.'}, status=404)
        if not jc.is_valid:
            return Response({'valid': False, 'error': 'Código expirado o revocado.'}, status=400)
        return Response({
            'valid': True,
            'empresa': jc.empresa.name,
            'expires_at': jc.expires_at.isoformat(),
            'max_uses': jc.max_uses,
            'used_count': jc.used_count
        })
